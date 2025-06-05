/** @type {import('next').NextConfig} */
const isProd = process.env.NODE_ENV === 'production';
const basePath = isProd ? '/Konnekt' : '';

const nextConfig = {
  basePath,
  assetPrefix: basePath,
  output: 'export',
  trailingSlash: true,
  images: {
    unoptimized: true,
  },
  // Force all asset paths to use the base path
  env: {
    NEXT_PUBLIC_BASE_PATH: basePath,
  },
};

module.exports = nextConfig;
