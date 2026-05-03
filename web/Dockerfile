FROM node:20-alpine AS builder

WORKDIR /app

COPY . .
RUN npm install
RUN npm run build

# 运行阶段
FROM node:20-alpine

WORKDIR /app

COPY --from=builder /app/.output .output
COPY --from=builder /app/node_modules node_modules
COPY --from=builder /app/package.json package.json

ENV NODE_ENV=production

CMD ["node", ".output/server/index.mjs"]