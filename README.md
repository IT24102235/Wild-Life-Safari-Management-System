# Safari Management System

A comprehensive Wildlife Safari Management System built with **Java 21 + Spring Boot 3.5.6** for the backend and **HTML + Tailwind CSS + vanilla JavaScript** for the frontend. The system implements role-based dashboards, operational workflows, booking state machines, and maintenance ticket management.

## Tech Stack

- **Backend:** Java 21, Spring Boot 3.5.6, Maven
- **Database:** Microsoft SQL Server (Docker)
- **Security:** JWT Authentication with Spring Security
- **Email:** Spring Mail with SMTP configuration
- **Database Migration:** Flyway
- **Frontend:** HTML + Tailwind CSS + vanilla JavaScript
- **Documentation:** OpenAPI 3 (Swagger UI)

## Features

### 🔐 Authentication & User Management
- **Role-based Access Control (RBAC)** with 9 user roles
- **JWT Authentication** with access and refresh tokens
- **Email OTP verification** for tourist registration
- **Password reset via OTP** for all users
- **Password policy enforcement** (≥8 chars, ≥1 special character)

### 👥 User Roles
- **Tourist** - Self-registers, creates bookings, manages profile
- **Admin** - Full system access, creates staff accounts
- **Booking Officer** - Manages booking requests and payment windows
- **Tour & Crew Manager** - Creates Driver/Guide accounts, manages allocations
- **Maintenance Officer** - Creates Mechanic accounts, manages repair tickets
- **Tour Package Builder** - Manages tour packages
- **Driver** - Views allocations, files maintenance tickets
- **Guide** - Views allocations, files maintenance tickets
- **Mechanic** - Updates repair ticket progress

### 📦 Core CRUD Operations
1. **Tourists** - Profile management with preferred languages
2. **Tour Packages** - 4 default packages (1-2 days, 5-10 people)
3. **Bookings** - State machine with edit/payment windows
4. **Allocations** - Driver/Guide/Jeep assignment to bookings
5. **Jeeps (Vehicles)** - Fleet management with status tracking
6. **Maintenance Tickets** - Repair workflow management

### 🔄 Business Workflows

#### Booking State Machine
```
REQUESTED → FORWARDED_TO_CREW → ALLOCATED → CONFIRMATION_SENT → PENDING_PAYMENT → CONFIRMED/CANCELLED/EXPIRED
```

#### Edit Windows & Timers
- **Default edit window:** 10 seconds (adjustable by Booking Officer)
- **Default payment window:** 20 seconds (adjustable by Booking Officer)
- **Automated expiry handling** with email notifications

#### Maintenance Workflow
- Drivers/Guides file tickets for vehicle issues
- Maintenance Officer assigns mechanics
- Vehicle status automatically updated (UNDER_REPAIR)
- Jeeps under repair excluded from allocations

## Quick Start

### Prerequisites
- Java 21
- Maven 3.6+
- Docker & Docker Compose

### 1. Start Database
```bash
cd "Safari Management System"
docker-compose up -d
```

### 2. Run Application
```bash
mvn clean install
mvn spring-boot:run
```

### 3. Access the System
- **Application:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **H2 Console:** http://localhost:8080/h2-console (if using H2 for testing)

## Default Credentials

### Admin Account
- **Username:** `admin1234`
- **Password:** `Admin@1234`

### Sample Staff Accounts (Created by seed data)
- **Booking Officer:** `booking_officer` / `BookingOfficer@123`
- **Crew Manager:** `crew_manager` / `CrewManager@123`
- **Maintenance Officer:** `maintenance_officer` / `MaintenanceOfficer@123`
- **Package Builder:** `package_builder` / `PackageBuilder@123`

### Sample Field Staff
- **Driver:** `john_driver` / `Driver@123`
- **Guide:** `sarah_guide` / `Guide@123`
- **Mechanic:** `mike_mechanic` / `Mechanic@123`

## API Documentation

### Authentication Endpoints
```http
POST /api/v1/auth/register/tourist     # Tourist registration
POST /api/v1/auth/verify-signup        # Email OTP verification
POST /api/v1/auth/login                # Login for all users
POST /api/v1/auth/forgot-password      # Password reset OTP
POST /api/v1/auth/reset-password       # Password reset with OTP
POST /api/v1/auth/refresh              # Token refresh
```

### Core Business Endpoints
```http
# Packages (Public)
GET    /api/v1/packages                # Browse packages (no auth required)
POST   /api/v1/packages               # Create package (Builder/Admin)

# Bookings (Tourist/Staff)
POST   /api/v1/bookings               # Create booking (Tourist)
PUT    /api/v1/bookings/{id}          # Update booking (Tourist, within edit window)
DELETE /api/v1/bookings/{id}          # Cancel booking (Tourist, within edit window)
POST   /api/v1/bookings/{id}/forward-to-crew  # Forward to crew (Booking Officer)

# Allocations (Crew Manager)
POST   /api/v1/allocations            # Create allocation
PUT    /api/v1/allocations/{id}       # Update allocation
DELETE /api/v1/allocations/{id}       # Cancel allocation

# Maintenance (Driver/Guide/Maintenance Officer)
POST   /api/v1/tickets                # File maintenance ticket
POST   /api/v1/tickets/{id}/assign/{mechanicId}  # Assign mechanic
POST   /api/v1/tickets/{id}/status    # Update ticket status

# Payments (Tourist)
POST   /api/v1/payments/{bookingId}/pay  # Process payment (mock)
```

## Database Schema

### Core Tables
- `users` - Central authentication table
- `tourists`, `drivers`, `guides`, `mechanics` - Role-specific profiles
- `tour_packages` - Safari packages (4 default)
- `bookings` - Booking requests with state machine
- `allocations` - Resource assignments (Driver/Guide/Jeep)
- `jeeps` - Vehicle fleet management
- `maintenance_tickets` - Repair workflow
- `payments` - Payment processing (mock)
- `languages` - 15 languages for guides/tourists

### Supporting Tables
- `notifications` - In-app notifications
- `otps` - OTP storage (hashed)
- `outbound_emails` - Email tracking (dev mode)
- `audit_log` - Critical action auditing

## Configuration

### Database Configuration (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=SafariDB
    username: sa
    password: YourStrong!Passw0rd
```

### Email Configuration
```yaml
spring:
  mail:
    host: ${SMTP_HOST:localhost}
    port: ${SMTP_PORT:587}
    username: ${SMTP_USERNAME:}
    password: ${SMTP_PASSWORD:}

app:
  email:
    enabled: ${EMAIL_ENABLED:false}  # Set to true for real emails
```

### Timer Configuration
```yaml
app:
  default-timers:
    edit-window-seconds: 10
    payment-window-seconds: 20
```

## Development Features

### Email Testing
When `EMAIL_ENABLED=false` (default), emails are:
- Logged to console with full content
- Stored in `outbound_emails` table for verification
- OTPs displayed in application logs

### Mock Payment System
- Card numbers ending in even digits → Success
- Card numbers ending in odd digits → Failure
- Default 80% success rate for random payments

### Scheduled Tasks
- **Payment expiry checking:** Every 30 seconds
- **Payment reminders:** Every minute (2 min before expiry)
- **OTP cleanup:** Daily at 2 AM
- **Pending allocation reminders:** Every 5 minutes

## Testing

### Running Tests
```bash
mvn test
```

### Manual Testing Flow
1. **Tourist Registration:**
   - Visit http://localhost:8080/signup.html
   - Register with email, check console for OTP
   - Verify email and login

2. **Booking Flow:**
   - Browse packages (no login required)
   - Create booking as tourist
   - Login as Booking Officer → forward to crew
   - Login as Crew Manager → create allocation
   - Return to Booking Officer → send confirmation
   - Tourist receives payment window

3. **Maintenance Flow:**
   - Login as Driver/Guide
   - File maintenance ticket for jeep
   - Login as Maintenance Officer → assign mechanic
   - Mechanic updates ticket status

## Production Deployment

### Environment Variables
```bash
export SPRING_PROFILES_ACTIVE=prod
export JWT_SECRET=your-production-jwt-secret
export SMTP_HOST=your-smtp-server
export SMTP_USERNAME=your-email
export SMTP_PASSWORD=your-password
export EMAIL_ENABLED=true
```

### Security Considerations
- Change default admin password
- Use strong JWT secret (256-bit)
- Enable HTTPS in production
- Configure proper CORS origins
- Set up proper SMTP for email delivery

## Architecture Highlights

### State Machine Implementation
- Booking status transitions with validation
- Automated timer-based state changes
- Email notifications at each stage

### Resource Management
- Real-time availability checking for jeeps
- Conflict prevention for driver/guide allocations
- Automatic status updates for maintenance

### Audit Trail
- All critical actions logged to `audit_log`
- Before/after JSON snapshots
- User action tracking

## Support & Documentation

- **API Documentation:** Available at `/swagger-ui.html` when running
- **Database Schema:** Auto-generated by Flyway migrations
- **Error Handling:** Comprehensive error responses with proper HTTP status codes
- **Logging:** Structured JSON logging with audit trails

## License

This Safari Management System is built as a demonstration project showcasing enterprise-level Java development practices with Spring Boot, security, and workflow management.
