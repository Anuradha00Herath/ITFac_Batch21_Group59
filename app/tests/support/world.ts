import { setWorldConstructor, World } from "@cucumber/cucumber";
import type { Browser, BrowserContext, Page, APIRequestContext, Dialog } from "playwright";

export class CustomWorld extends World {
  browser?: Browser;
  context?: BrowserContext;
  page?: Page;

  request?: APIRequestContext;

  adminToken?: string;
  userToken?: string;

  // useful shared state
  beforeStock?: number;
  createdSaleId?: string;
  capturedSaleId?: string;

  lastStatus?: number;
  lastBodyText?: string;

  plantId?: string;
  exactStock?: number;
  minStock?: number;

  _pendingDialog?: Dialog;
  lastDialogMessage?: string;
}

setWorldConstructor(CustomWorld);
