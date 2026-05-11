import { readdir, readlink, cp, rm } from 'node:fs/promises'
import { relative, resolve, isAbsolute } from 'node:path'
import { existsSync } from 'node:fs'

const OUTPUT_MODULES = '.output/server/node_modules'

/**
 * Recursively find all symlinks under a directory.
 * Returns array of { linkPath, target }
 */
async function findSymlinks(dir) {
  const results = []
  let entries
  try {
    entries = await readdir(dir, { withFileTypes: true })
  } catch {
    return results
  }
  for (const entry of entries) {
    const fullPath = resolve(dir, entry.name)
    if (entry.isSymbolicLink()) {
      const target = await readlink(fullPath)
      results.push({ linkPath: fullPath, target })
    } else if (entry.isDirectory()) {
      results.push(...await findSymlinks(fullPath))
    }
  }
  return results
}

async function main() {
  if (!existsSync(OUTPUT_MODULES)) {
    console.log('No .output/server/node_modules found, skipping.')
    return
  }

  const allSymlinks = await findSymlinks(OUTPUT_MODULES)

  if (allSymlinks.length === 0) {
    console.log('No symlinks found in output node_modules.')
    return
  }

  console.log(`Found ${allSymlinks.length} symlink(s) in output node_modules:`)

  for (const { linkPath, target } of allSymlinks) {
    // Resolve the target: if it's already absolute, use it directly;
    // otherwise resolve relative to the symlink's directory.
    const targetAbsolute = resolve(linkPath, '..', target)

    if (!existsSync(targetAbsolute)) {
      console.log(`  [SKIP] ${relative(process.cwd(), linkPath)} -> ${target} (target missing: ${targetAbsolute})`)
      continue
    }

    console.log(`  [FIX]  ${relative(process.cwd(), linkPath)}`)

    // Remove the symlink (use rm to avoid following symlinks)
    await rm(linkPath, { recursive: false, force: true })

    // Copy the actual target directory into its place
    await cp(targetAbsolute, linkPath, { recursive: true })
  }

  console.log('Done. Output node_modules is now portable.')
}

main().catch((err) => {
  console.error('Failed to fix output symlinks:', err)
  process.exit(1)
})
