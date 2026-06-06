const fs = require('fs').promises;
const path = require('path');

const srcDir = path.resolve(__dirname, '../..', 'frontend', 'dist');
const destDir = path.resolve(__dirname, '..', 'dist');

async function copyDirectory(src, dest) {
  await fs.mkdir(dest, { recursive: true });
  const entries = await fs.readdir(src, { withFileTypes: true });

  for (const entry of entries) {
    const srcPath = path.join(src, entry.name);
    const destPath = path.join(dest, entry.name);

    if (entry.isDirectory()) {
      await copyDirectory(srcPath, destPath);
    } else {
      await fs.copyFile(srcPath, destPath);
    }
  }
}

copyDirectory(srcDir, destDir)
  .then(() => {
    console.log('Copied frontend/dist into desktop/dist successfully.');
  })
  .catch((error) => {
    console.error('Failed to copy frontend/dist:', error);
    process.exit(1);
  });
