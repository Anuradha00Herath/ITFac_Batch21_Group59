import { Given, When, Then } from "@cucumber/cucumber";
import { expect } from "playwright/test";
import { CustomWorld } from "../../support/world";
import {
  creds,
  API_GET_PLANT,
  API_SELL,
  API_SALE_BY_ID,
  API_SALES_ALL,
  API_SALES_PAGE
} from "../../support/config";
import { loginAndGetToken } from "../../support/auth";

function req(world: CustomWorld) {
  if (!world.request) throw new Error("API request context not initialized. Did you run with @api and hooks?");
  return world.request;
}

function tokenFor(world: CustomWorld, role: "admin" | "user") {
  const t = role === "admin" ? world.adminToken : world.userToken;
  if (!t) throw new Error(`No token for role=${role}. Authenticate first.`);
  return t;
}

Given("I authenticate as {string} via API", async function (this: CustomWorld, role: string) {
  const r = req(this);
  const c = creds(role as "admin" | "user");
  const token = await loginAndGetToken(r, c.username, c.password);

  if (role === "admin") this.adminToken = token;
  else this.userToken = token;
});

Given(
  'a plant exists with id {string} and current stock is at least {int}',
  async function (this: CustomWorld, plantId: string, min: number) {
    const r = req(this);
    const t = this.adminToken || this.userToken;
    if (!t) throw new Error("No token available.");

    const res = await r.get(API_GET_PLANT(plantId), {
      headers: { Authorization: `Bearer ${t}` }
    });

    expect(res.ok()).toBeTruthy();
    const plant = await res.json();

    const stock = Number(plant.stock ?? plant.quantity ?? plant.availableStock);
    this.beforeStock = stock;

    expect(stock).toBeGreaterThanOrEqual(min);
  }
);

When(
  "I sell quantity {int} of plant {string}",
  async function (this: CustomWorld, qty: number, plantId: string) {
    const r = req(this);
    const t = tokenFor(this, "admin"); // this scenario expects admin

    const res = await r.post(API_SELL, {
      headers: { Authorization: `Bearer ${t}` },
      data: { plantId, quantity: qty }
    });

    this.lastStatus = res.status();
    this.lastBodyText = await res.text();

    if (res.ok()) {
      const json = JSON.parse(this.lastBodyText);
      this.createdSaleId = json.id || json.saleId;
    }
  }
);

Then("the sale should be created successfully", async function (this: CustomWorld) {
  expect(this.lastStatus).toBeGreaterThanOrEqual(200);
  expect(this.lastStatus).toBeLessThan(300);
  expect(this.createdSaleId).toBeTruthy();
});

Then(
  'the plant {string} stock should be decreased by {int}',
  async function (this: CustomWorld, plantId: string, qty: number) {
    const r = req(this);
    const t = tokenFor(this, "admin");

    const res = await r.get(API_GET_PLANT(plantId), {
      headers: { Authorization: `Bearer ${t}` }
    });
    expect(res.ok()).toBeTruthy();
    const plant = await res.json();

    const afterStock = Number(plant.stock ?? plant.quantity ?? plant.availableStock);
    expect(typeof this.beforeStock).toBe("number");
    expect(afterStock).toBe((this.beforeStock as number) - qty);
  }
);

Given(
  'I create a sale for plant {string} with quantity {int} via API',
  async function (this: CustomWorld, plantId: string, qty: number) {
    const r = req(this);
    const t = tokenFor(this, "admin");

    const res = await r.post(API_SELL, {
      headers: { Authorization: `Bearer ${t}` },
      data: { plantId, quantity: qty }
    });

    expect(res.ok()).toBeTruthy();
    const json = await res.json();
    this.createdSaleId = json.id || json.saleId;

    if (!this.createdSaleId) throw new Error("Sale created but no id returned (id/saleId missing).");
  }
);

When("I delete the created sale via API", async function (this: CustomWorld) {
  const r = req(this);
  const t = tokenFor(this, "admin");
  if (!this.createdSaleId) throw new Error("No createdSaleId to delete.");

  const res = await r.delete(API_SALE_BY_ID(this.createdSaleId), {
    headers: { Authorization: `Bearer ${t}` }
  });

  this.lastStatus = res.status();
  this.lastBodyText = await res.text();
});

Then("the sale should be deleted successfully", async function (this: CustomWorld) {
  expect(this.lastStatus).toBeGreaterThanOrEqual(200);
  expect(this.lastStatus).toBeLessThan(300);
});

Then(
  'fetching that sale by id should return {string}',
  async function (this: CustomWorld, expected: string) {
    const r = req(this);
    const t = tokenFor(this, "admin");
    if (!this.createdSaleId) throw new Error("No createdSaleId to fetch.");

    const res = await r.get(API_SALE_BY_ID(this.createdSaleId), {
      headers: { Authorization: `Bearer ${t}` }
    });

    if (expected.toLowerCase().includes("not found")) {
      expect([404, 410]).toContain(res.status());
    } else {
      expect(res.ok()).toBeTruthy();
    }
  }
);

Then("the API should respond with status {int}", async function (this: CustomWorld, code: number) {
  expect(this.lastStatus).toBe(code);
});

Then('the error message should contain {string}', async function (this: CustomWorld, text: string) {
  const body = (this.lastBodyText || "").toLowerCase();
  expect(body).toContain(text.toLowerCase());
});

When(
  "I attempt to sell quantity {int} of plant {string} using user credentials",
  async function (this: CustomWorld, qty: number, plantId: string) {
    const r = req(this);
    const t = tokenFor(this, "user");

    const res = await r.post(API_SELL, {
      headers: { Authorization: `Bearer ${t}` },
      data: { plantId, quantity: qty }
    });

    this.lastStatus = res.status();
    this.lastBodyText = await res.text();
  }
);

When("I request all sales", async function (this: CustomWorld) {
  const r = req(this);
  const t = tokenFor(this, "user");

  const res = await r.get(API_SALES_ALL, {
    headers: { Authorization: `Bearer ${t}` }
  });

  this.lastStatus = res.status();
  this.lastBodyText = await res.text();
});

Then("the response should contain a list of sales", async function (this: CustomWorld) {
  const json = JSON.parse(this.lastBodyText || "null");
  // Accept either array response or wrapper {content:[...]}
  const list = Array.isArray(json) ? json : json.content;
  expect(Array.isArray(list)).toBeTruthy();
});

When(
  'I request sales page {int} size {int} sorted by {string} {string}',
  async function (this: CustomWorld, page: number, size: number, sortField: string, dir: string) {
    const r = req(this);
    const t = tokenFor(this, "user");

    const url = `${API_SALES_PAGE}?page=${page}&size=${size}&sort=${encodeURIComponent(sortField)},${encodeURIComponent(dir)}`;
    const res = await r.get(url, { headers: { Authorization: `Bearer ${t}` } });

    this.lastStatus = res.status();
    this.lastBodyText = await res.text();
  }
);

Then("the response should contain paginated sales data", async function (this: CustomWorld) {
  const json = JSON.parse(this.lastBodyText || "null");
  // Common Spring pagination fields: content, totalElements, totalPages, number, size
  expect(json).toBeTruthy();
  expect(Array.isArray(json.content)).toBeTruthy();
});

When("I request all sales without authentication", async function (this: CustomWorld) {
  const r = req(this);
  const res = await r.get(API_SALES_ALL);
  this.lastStatus = res.status();
  this.lastBodyText = await res.text();
});

When("I request sales pagination endpoint without parameters", async function (this: CustomWorld) {
  const r = req(this);
  const t = tokenFor(this, "user");

  const res = await r.get(API_SALES_PAGE, {
    headers: { Authorization: `Bearer ${t}` }
  });

  this.lastStatus = res.status();
  this.lastBodyText = await res.text();
});

Given("at least one sale exists and I capture a valid sale id", async function (this: CustomWorld) {
  const r = req(this);
  const t = tokenFor(this, "user");

  const res = await r.get(API_SALES_ALL, {
    headers: { Authorization: `Bearer ${t}` }
  });
  expect(res.ok()).toBeTruthy();
  const json = await res.json();

  const list = Array.isArray(json) ? json : json.content;
  if (!list || list.length === 0) throw new Error("No sales exist to capture an id.");

  this.capturedSaleId = list[0].id || list[0].saleId;
  if (!this.capturedSaleId) throw new Error("Sale object does not contain id/saleId.");
});

When("I request sale by that id", async function (this: CustomWorld) {
  const r = req(this);
  const t = tokenFor(this, "user");
  if (!this.capturedSaleId) throw new Error("No capturedSaleId.");

  const res = await r.get(API_SALE_BY_ID(this.capturedSaleId), {
    headers: { Authorization: `Bearer ${t}` }
  });

  this.lastStatus = res.status();
  this.lastBodyText = await res.text();
});

Then("the response should contain the sale details", async function (this: CustomWorld) {
  const json = JSON.parse(this.lastBodyText || "null");
  expect(json).toBeTruthy();
});
