/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  experimental: {
    outputFileTracingExcludes: {
      '*': [
        'android/**/*',
        'build.gradle.kts',
        'settings.gradle.kts',
        'gradle.properties',
        'gradle/**/*',
        '.gradle/**/*',
      ],
    },
  },
  webpack: (config) => {
    config.watchOptions = {
      ...config.watchOptions,
      ignored: [
        '**/android/**',
        '**/.gradle/**',
        '**/gradle/**',
      ],
    };
    return config;
  },
};

export default nextConfig;
