FROM node:14

WORKDIR /usr/src/app

COPY package*.json ./

RUN npm install

COPY . .

ENV PORT=4001
EXPOSE 4001

ENTRYPOINT ["node", "server.js"]
