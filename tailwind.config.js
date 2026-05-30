/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{js,ts,jsx,tsx,mdx}",
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        background: "#0E0E11",
        deepSurface: "#16161C",
        cozyCard: "#1E1E26",
        primaryAmber: "#FFB300",
        accentPurple: "#B388FF",
        textMuted: "#9CA3AF",
        validGreen: "#00E676"
      },
    },
  },
  plugins: [],
}
