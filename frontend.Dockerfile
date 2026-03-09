# Build stage
FROM node:22-alpine AS builder

WORKDIR /build

# Copy package files
COPY frontend/package*.json ./

# Install dependencies
RUN npm install

# Copy source code
COPY frontend .

# Build application
RUN npm run build

# Runtime stage
FROM node:22-alpine

WORKDIR /app

# Install serve to run the Angular app
RUN npm install -g serve

# Copy built application from builder
COPY --from=builder /build/dist/frontend1/browser ./dist

EXPOSE 4200

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=20s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:4200 || exit 1

# Run the application
CMD ["serve", "-s", "dist", "-l", "4200"]
