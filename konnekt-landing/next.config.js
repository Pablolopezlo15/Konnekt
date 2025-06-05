/** @type {import('next').NextConfig} */
const isProd = process.env.NODE_ENV === 'production';
const basePath = process.env.NEXT_PUBLIC_BASE_PATH || '';

const nextConfig = {
  output: 'export',
  trailingSlash: true,
  skipTrailingSlashRedirect: true,
  distDir: 'out',
  basePath: isProd ? basePath : '',
  assetPrefix: isProd ? basePath : '',
  images: {
    unoptimized: true,
  },
  // Remove rewrites since they don't work with static export
};

console.log('Next.js config:', { isProd, basePath, assetPrefix: isProd ? basePath : '' });

module.exports = nextConfig;
