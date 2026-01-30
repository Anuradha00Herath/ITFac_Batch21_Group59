import "dotenv/config";

export const BASE_URL = process.env.BASE_URL || "http://localhost:8080";

// ---- API endpoints (EDIT to match your backend) ----
export const API_LOGIN = process.env.API_LOGIN || "/api/auth/login";
export const API_GET_PLANT = (id: string) => `/api/plants/${id}`;
export const API_SELL = process.env.API_SELL || "/api/sales"; // POST { plantId, quantity }
export const API_SALE_BY_ID = (id: string) => `/api/sales/${id}`;
export const API_SALES_ALL = process.env.API_SALES_ALL || "/api/sales";
export const API_SALES_PAGE = process.env.API_SALES_PAGE || "/api/sales/page"; // GET with query params

export function creds(role: "admin" | "user") {
  if (role === "admin") {
    return {
      username: "admin",
      password: "admin123"
    };
  }
  return {
    username: "testuser",
    password: "test123"
  };
}
