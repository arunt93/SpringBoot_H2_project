# Docker Setup for User Management System

## Prerequisites
- Docker and Docker Compose installed on your system

## Quick Start

### 1. Build and Run with Docker Compose
```bash
# Build and start all services
docker-compose up --build

# Run in detached mode
docker-compose up --build -d

# View logs
docker-compose logs -f
```

### 2. Set JWT Secret (Recommended for Production)
```bash
# Set environment variable before starting
export JWT_SECRET="your-super-secure-random-secret-key-here"
docker-compose up --build
```

### 3. Access the Application
- **Application**: http://localhost:8092
- **MySQL Database**: localhost:3306
- **Health Check**: http://localhost:8092/actuator/health

## Services

### MySQL Database
- **Container Name**: user-management-mysql
- **Database**: User_Database
- **Username**: usermanagement
- **Password**: UserPassword123
- **Root Password**: MyStrongPassword123

### Spring Boot Application
- **Container Name**: user-management-app
- **Port**: 8092
- **Health Check**: Every 30 seconds

## Docker Commands

### Stop Services
```bash
docker-compose down
```

### Stop and Remove Volumes
```bash
docker-compose down -v
```

### View Running Containers
```bash
docker-compose ps
```

### Access Application Container
```bash
docker-compose exec app bash
```

### Access MySQL Container
```bash
docker-compose exec mysql mysql -u root -p
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `JWT_SECRET` | Default secret | JWT signing secret |
| `SPRING_DATASOURCE_URL` | MySQL URL | Database connection URL |
| `SPRING_DATASOURCE_USERNAME` | usermanagement | Database username |
| `SPRING_DATASOURCE_PASSWORD` | UserPassword123 | Database password |

## API Endpoints

### Authentication
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/register` - User registration

### User Management (Requires JWT)
- `GET /api/v1/users` - Get all users
- `GET /api/v1/users/get-by-id?id={id}` - Get user by ID
- `POST /api/v1/users` - Create user
- `PUT /api/v1/users/update?id={id}` - Update user
- `DELETE /api/v1/users/delete?id={id}` - Delete user

### Role Management (Public)
- `GET /api/v1/roles` - Get all roles
- `POST /api/v1/roles` - Create role

## Health Checks

Both services include health checks:
- MySQL: Checks database connectivity
- Application: Checks API endpoint availability

## Production Considerations

1. **Change Default Passwords**: Update MySQL passwords in production
2. **Use Environment Variables**: Don't hardcode secrets
3. **Network Security**: Consider using private networks
4. **Volume Backup**: Implement database backup strategy
5. **Resource Limits**: Add memory/CPU limits in production

## Troubleshooting

### Database Connection Issues
```bash
# Check MySQL container logs
docker-compose logs mysql

# Test database connection
docker-compose exec mysql mysql -u usermanagement -p User_Database
```

### Application Issues
```bash
# Check application logs
docker-compose logs app

# Access application container
docker-compose exec app bash
```

### Port Conflicts
If port 8092 or 3306 are already in use, modify the ports in `docker-compose.yml`:
```yaml
ports:
  - "8093:8092"  # Change host port to 8093
```
