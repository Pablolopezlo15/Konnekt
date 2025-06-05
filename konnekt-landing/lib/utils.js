// Utility function to get the correct image path for GitHub Pages
export function getImagePath(imagePath) {
  const basePath = process.env.NEXT_PUBLIC_BASE_PATH || '';
  // Ensure the path starts with /
  const normalizedPath = imagePath.startsWith('/') ? imagePath : `/${imagePath}`;
  return `${basePath}${normalizedPath}`;
}

// Alternative: get base path for any asset
export function getAssetPath(assetPath) {
  const basePath = process.env.NEXT_PUBLIC_BASE_PATH || '';
  const normalizedPath = assetPath.startsWith('/') ? assetPath : `/${assetPath}`;
  return `${basePath}${normalizedPath}`;
}
