import { GoogleGenAI } from "@google/genai";

const SESSION_KEY = "feetstudio.v4.geminiKey";
let memoryKey = "";

const storageAvailable = () =>
  typeof window !== "undefined" && typeof window.sessionStorage !== "undefined";

export const getGeminiApiKey = (): string => {
  if (memoryKey) return memoryKey;
  if (storageAvailable()) {
    memoryKey = (window.sessionStorage.getItem(SESSION_KEY) || "").trim();
  }
  return memoryKey;
};

export const setGeminiApiKey = (key: string): void => {
  memoryKey = key.trim();
  if (!storageAvailable()) return;
  if (memoryKey) window.sessionStorage.setItem(SESSION_KEY, memoryKey);
  else window.sessionStorage.removeItem(SESSION_KEY);
};

export const clearGeminiApiKey = (): void => setGeminiApiKey("");

export const createGeminiClient = (): GoogleGenAI => {
  const apiKey = getGeminiApiKey();
  if (!apiKey) {
    throw new Error("Geen Gemini API-key ingesteld. Open Render en vul je eigen API-key in.");
  }
  return new GoogleGenAI({ apiKey });
};
