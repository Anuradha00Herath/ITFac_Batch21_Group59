import type { APIRequestContext } from "playwright";
import { API_LOGIN } from "./config";

export async function loginAndGetToken(
  request: APIRequestContext,
  username: string,
  password: string
): Promise<string> {
  const res = await request.post(API_LOGIN, {
    data: { username, password }
  });

  if (!res.ok()) {
    throw new Error(`Login failed (${res.status()}): ${await res.text()}`);
  }

  const json = await res.json();
  // Adjust the key if your backend returns accessToken/jwt/etc.
  const token = json.token || json.accessToken || json.jwt;
  if (!token) throw new Error("Login response does not contain token/accessToken/jwt");
  return token;
}
