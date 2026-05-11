FROM node:20-alpine

WORKDIR /app

COPY .output .output

ENV NODE_ENV=production

CMD ["node", ".output/server/index.mjs"]