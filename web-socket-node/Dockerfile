FROM node:14 as builder

WORKDIR /usr/src/app

COPY package*.json ./

RUN npm install

COPY . .

FROM gcr.io/distroless/nodejs:14 as runner

WORKDIR /usr/src/app

ENV PORT=4001

COPY --from=builder /usr/src/app .

CMD ["./server.js"]
