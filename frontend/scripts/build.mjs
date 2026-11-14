import { spawnSync } from 'node:child_process'

function run(args) {
  const result = spawnSync(process.execPath, args, { stdio: 'inherit' })
  if (result.error) {
    console.error(result.error)
    process.exit(1)
  }
  if (result.status !== 0) {
    process.exit(result.status ?? 1)
  }
}

run(['./node_modules/vue-tsc/bin/vue-tsc.js', '--noEmit'])
run(['./node_modules/vite/bin/vite.js', 'build'])
