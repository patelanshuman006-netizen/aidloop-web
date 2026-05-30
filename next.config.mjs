/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  experimental: {
    outputFileTracingExcludes: {
      '*': [
        'app/src/**/*',
        'app/build/**/*',
        'app/build.gradle.kts',
        'build.gradle.kts',
        'settings.gradle.kts',
        'gradle.properties',
        'gradle/**/*',
        '.gradle/**/*',
      ],
    },
  },
};

export default nextConfig;
