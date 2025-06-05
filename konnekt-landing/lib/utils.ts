import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

// Utility function to get the correct image path for GitHub Pages
export function getImagePath(imagePath: string): string {
  const basePath = process.env.NEXT_PUBLIC_BASE_PATH || "";
  const normalizedPath = imagePath.startsWith("/")
    ? imagePath
    : `/${imagePath}`;
  return `${basePath}${normalizedPath}`;
}

// Alternative: get base path for any asset
export function getAssetPath(assetPath: string): string {
  const basePath = process.env.NEXT_PUBLIC_BASE_PATH || "";
  const normalizedPath = assetPath.startsWith("/")
    ? assetPath
    : `/${assetPath}`;
  return `${basePath}${normalizedPath}`;
}
