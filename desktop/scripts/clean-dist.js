const fs = require('fs').promises;
const path = require('path');

const distDir = path.resolve(__dirname, '..', 'dist');

async function clean() {
  await fs.rm(distDir, { recursive: true, force: true });
  console.log('Cleaned desktop/dist directory.');
}

clean().catch((error) => {
  console.error('Failed to clean desktop/dist:', error);
  process.exit(1);
});
