import { Before, After } from "@cucumber/cucumber";
import { chromium, request as pwRequest } from "playwright";
import { CustomWorld } from "./world";
import { BASE_URL } from "./config";
import fs from "fs";

Before({ tags: "@ui" }, async function (this: CustomWorld) {
  this.browser = await chromium.launch({ headless: false });
  this.context = await this.browser.newContext();
  this.page = await this.context.newPage();
});

Before({ tags: "@api" }, async function (this: CustomWorld) {
  this.request = await pwRequest.newContext({
    baseURL: BASE_URL,
    extraHTTPHeaders: { "Content-Type": "application/json" }
  });
});

After({ tags: "@ui" }, async function (this: CustomWorld, scenario) {
  try {
    if (scenario.result?.status === "FAILED" && this.page) {
      fs.mkdirSync("tests/screenshots", { recursive: true });
      await this.page.screenshot({
        path: `tests/screenshots/${Date.now()}-FAILED.png`,
        fullPage: true
      });
    }
  } finally {
    await this.context?.close();
    await this.browser?.close();
  }
});

After({ tags: "@api" }, async function (this: CustomWorld) {
  await this.request?.dispose();
});
