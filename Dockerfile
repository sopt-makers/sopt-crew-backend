FROM node AS builder
WORKDIR /app
COPY . .
RUN npm install
RUN npm run build


FROM node:alpine
WORKDIR /app
COPY --from=builder /app ./
EXPOSE 3000
CMD ["npm", "run", "start:prod"]