# Redis Memory Layer

- Current configuration uses the default `redis:7-alpine` settings.
- Redis is used for storing `ConversationMemory` using `LIST` structures.
- Persistence is handled via the `redis_data` volume in `docker-compose.yml`.

### Future Customization
To customize Redis (e.g., adding a password or specialized eviction policy):
1. Create a `redis.conf` in this directory.
2. Mount it as a volume in `docker-compose.yml`:
   `- ./redis/redis.conf:/usr/local/etc/redis/redis.conf`
3. Update the command to: `redis-server /usr/local/etc/redis/redis.conf`
