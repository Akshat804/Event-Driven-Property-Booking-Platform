# 🏠 Event-Driven Property Booking Platform

## 📌 Overview

A scalable property booking backend designed to demonstrate distributed systems concepts such as event-driven workflows, distributed locking, CQRS-inspired read models, and Saga-based transaction orchestration.

The platform supports reservation management, availability tracking, and concurrent booking handling. Redis-based distributed locks prevent double bookings, while Saga-driven workflows coordinate booking confirmations and compensating actions. Redis-backed read models enable efficient query performance with reduced database load.

## ✨ Key Features

* Reservation creation, confirmation, and cancellation workflows
* Date-wise property availability management
* Redis-based distributed locking for concurrency control
* Saga-driven booking confirmation and compensation handling
* CQRS-inspired Redis read models for optimized reads
* Idempotent booking request processing
* Event-driven asynchronous workflow execution

## 🛠️ Tech Stack

**Backend:** Java, Spring Boot, Spring Data JPA

**Database:** MySQL

**Caching & Messaging:** Redis

**Build Tool:** Gradle

## 🔑 Concepts Implemented

* Distributed Locking
* Saga Pattern
* CQRS Read Models
* Event-Driven Architecture
* Idempotency
* Redis Caching
* Transaction Management
* Asynchronous Processing
