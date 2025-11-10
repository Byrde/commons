# Action Plan - Byrde Commons Improvements

**Status**: Ready for review and implementation  
**Priority**: High - Affects testability and maintainability

---

## Immediate Actions (Critical)

### 1. Verify Build ⚡

**Status**: Build configuration fixed, awaiting verification

```bash
cd /Users/martin/workspace/byrde/commons
sbt compile
sbt test
```

**Expected Result**: Clean compilation, existing tests pass

**If Compilation Fails**: 
- Check Scala version compatibility
- Verify dependency versions
- Review error messages and address

---

### 2. Review CI/CD Configuration ⚡

**Status**: Created `.github/workflows/ci-cd.yml`

**Action Items**:
- [ ] Review workflow configuration
- [ ] Ensure GitHub Packages is enabled on repository
- [ ] Test workflow by pushing to a branch
- [ ] Verify matrix build publishes all modules

**Documentation**: The workflow automatically:
- Builds and tests on push/PR
- Publishes to GitHub Packages on release or master/main push
- Uses GITHUB_TOKEN (no manual secret configuration needed)

---

### 3. Stage Git Changes 📝

**Modified Files**:
- `build.sbt` - Removed slick module references

**New Files**:
- `.github/workflows/ci-cd.yml` - CI/CD configuration
- `CODEBASE_REVIEW.md` - Comprehensive review document
- `REFACTORING_EXAMPLES.md` - Refactoring patterns and examples
- `ACTION_PLAN.md` - This file

**Git Commands**:
```bash
# Review changes
git status
git diff build.sbt

# Stage changes
git add build.sbt
git add .github/
git add CODEBASE_REVIEW.md
git add REFACTORING_EXAMPLES.md
git add ACTION_PLAN.md

# Commit
git commit -m "feat: remove slick module and add CI/CD configuration

- Remove slick module from build.sbt (directory already deleted)
- Add GitHub Actions workflow for CI/CD
- Add comprehensive codebase review documentation
- Add refactoring examples for interface design improvements"

# Push (will trigger CI/CD)
git push origin master
```

---

## Phase 1: Testing Infrastructure (Week 1-2)

### Priority 1.1: Add Test Infrastructure

**Goal**: Establish testing foundation for all modules

**Tasks**:
- [ ] Add ScalaTest configuration to all modules
- [ ] Create test utility classes
- [ ] Set up test fixtures

**Estimate**: 2-3 days

---

### Priority 1.2: Add Tests for Core Modules

**Goal**: Test coverage for critical functionality

**Order of Implementation**:

1. **Logging Module Tests** (Easiest - 1 day)
   ```scala
   // logging/src/test/scala/org/byrde/logging/LoggerSpec.scala
   class LoggerSpec extends AnyFlatSpec {
     "Logger interface" should "allow test implementations" in {
       val testLogger = new TestLogger
       testLogger.logInfo("test")
       assert(testLogger.messages.contains("test"))
     }
   }
   ```

2. **Commons Module Tests** (2 days)
   - Test support traits (EitherSupport, FutureSupport, etc.)
   - Expand existing value type tests
   - Test edge cases

3. **Redis-Client Tests** (3 days)
   - Unit tests with mocked RedisService
   - Test serialization/deserialization
   - Test error handling

4. **Jedis-Client Tests** (2 days)
   - Integration tests with embedded Redis or Testcontainers
   - Test connection pooling
   - Test configuration parsing

**Estimate**: 8 days total

---

## Phase 2: Interface Refactoring (Week 3-4)

### Priority 2.1: PubSub Module Refactoring (Critical)

**Goal**: Extract trait interfaces, improve testability

**Implementation Steps**:

1. **Create Trait Interfaces** (1 day)
   - Define `MessagePublisher[T]` trait
   - Define `MessageSubscriber[T]` trait
   - Update error types to use Either

2. **Refactor Concrete Implementation** (2 days)
   - Create `GooglePubSubPublisher`
   - Create `GooglePubSubSubscriber`
   - Move state management to concrete classes
   - Maintain backward compatibility

3. **Add Test Implementation** (1 day)
   - Create `InMemoryPublisher`
   - Create `InMemorySubscriber`
   - Add helper methods for testing

4. **Add Tests** (2 days)
   - Unit tests with in-memory implementations
   - Integration tests (optional)

**Files to Create/Modify**:
```
pubsub/src/main/scala/org/byrde/pubsub/
├── MessagePublisher.scala (new trait)
├── MessageSubscriber.scala (new trait)
├── google/
│   ├── GooglePubSubPublisher.scala (new)
│   └── GooglePubSubSubscriber.scala (new)
└── test/
    ├── InMemoryPublisher.scala (new)
    └── InMemorySubscriber.scala (new)

pubsub/src/test/scala/org/byrde/pubsub/
├── PublisherSpec.scala (new)
└── SubscriberSpec.scala (new)
```

**Estimate**: 6 days

---

### Priority 2.2: SMTP Module Refactoring

**Goal**: Extract trait interface, add error handling

**Implementation Steps**:

1. **Create Trait Interface** (0.5 days)
   - Define `EmailClient` trait
   - Define `SmtpError` types
   - Update return types to use Either

2. **Refactor Concrete Implementation** (1 day)
   - Rename `SmtpClient` to `JavaMailClient`
   - Implement `EmailClient` trait
   - Add proper error handling

3. **Add Test Implementation** (0.5 days)
   - Create `TestEmailClient`
   - Add helper methods for assertions

4. **Add Tests** (1 day)
   - Unit tests with test implementation
   - Test error scenarios

**Files to Create/Modify**:
```
smtp/src/main/scala/org/byrde/smtp/
├── EmailClient.scala (new trait)
├── SmtpError.scala (new)
├── impl/
│   └── JavaMailClient.scala (renamed from SmtpClient)
└── test/
    └── TestEmailClient.scala (new)

smtp/src/test/scala/org/byrde/smtp/
└── EmailClientSpec.scala (new)
```

**Estimate**: 3 days

---

### Priority 2.3: Redis Serialization Refactoring (Optional)

**Goal**: Extract serialization logic, improve type safety

**Implementation Steps**:

1. **Define Serialization Interface** (0.5 days)
2. **Implement CirceRedisSerializer** (1.5 days)
3. **Add Tests** (1 day)
4. **Migrate RedisClient to use new serializer** (1 day)

**Estimate**: 4 days (can be deferred to Phase 3)

---

## Phase 3: Documentation & Polish (Week 5)

### Priority 3.1: API Documentation

**Tasks**:
- [ ] Add ScalaDoc to all public traits and classes
- [ ] Document expected behavior and edge cases
- [ ] Add examples to ScalaDoc

**Modules** (priority order):
1. commons (2 days)
2. logging (0.5 days)
3. redis-client (1 day)
4. pubsub (1 day)
5. smtp (0.5 days)

**Estimate**: 5 days

---

### Priority 3.2: Module READMEs

**Tasks**:
- [ ] Update each module's README with:
  - Purpose and use cases
  - Installation instructions
  - Configuration examples
  - Usage examples
  - Common patterns

**Template**:
```markdown
# Module Name

## Purpose
Brief description of what the module does

## Installation
```sbt
libraryDependencies += "org.byrde" %% "module-name" % "version"
```

## Configuration
Example configuration with Typesafe Config

## Usage
Basic usage examples

## Testing
How to test code that uses this module

## Common Patterns
Recommended usage patterns
```

**Estimate**: 2 days

---

## Phase 4: Advanced Improvements (Week 6+)

### Priority 4.1: Standardize Error Handling

**Goal**: Use Either consistently across all modules

**Tasks**:
- [ ] Document error handling patterns
- [ ] Ensure all public APIs use Either or Future[Either]
- [ ] Create common error base traits

---

### Priority 4.2: Configuration Validation

**Goal**: Validate configuration at startup

**Tasks**:
- [ ] Add validation to all config companion objects
- [ ] Return Either[ConfigError, Config] instead of throwing
- [ ] Add validation tests

---

### Priority 4.3: Performance Testing

**Goal**: Establish performance baselines

**Tasks**:
- [ ] Add JMH benchmarks for critical paths
- [ ] Document performance characteristics
- [ ] Set up performance regression testing

---

## Success Metrics

### Phase 1 Complete When:
- ✅ All modules compile cleanly
- ✅ All modules have test suites
- ✅ Test coverage > 60% for critical paths
- ✅ CI/CD pipeline passes

### Phase 2 Complete When:
- ✅ PubSub module has trait interfaces
- ✅ SMTP module has trait interface
- ✅ All new interfaces have test implementations
- ✅ All modules follow consistent patterns
- ✅ Test coverage > 75%

### Phase 3 Complete When:
- ✅ All public APIs have ScalaDoc
- ✅ All modules have comprehensive READMEs
- ✅ Examples are provided for all major use cases

### Phase 4 Complete When:
- ✅ Error handling is consistent across all modules
- ✅ Configuration validation is comprehensive
- ✅ Performance benchmarks are established

---

## Resource Requirements

### Development Team
- **Phase 1**: 1 developer, full-time, 2 weeks
- **Phase 2**: 1-2 developers, full-time, 2 weeks
- **Phase 3**: 1 developer, part-time, 1 week
- **Phase 4**: 1 developer, part-time, ongoing

### Infrastructure
- GitHub Actions (free for public repos)
- GitHub Packages (free for public repos)
- Optional: Redis instance for integration tests
- Optional: Google Pub/Sub emulator for integration tests

---

## Risk Assessment

### Low Risk
- Adding tests (no breaking changes)
- Adding documentation (no code changes)
- CI/CD implementation (infrastructure only)

### Medium Risk
- Refactoring with backward compatibility
- Configuration changes (might affect existing users)

### High Risk
- Breaking API changes (require major version bump)
- Removing deprecated code (coordinate with users)

### Mitigation Strategies
1. **Maintain backward compatibility** during refactoring
2. **Deprecate before removing** old APIs
3. **Version bump appropriately** (semantic versioning)
4. **Communicate changes** via release notes
5. **Provide migration guides** for breaking changes

---

## Decision Log

### Decisions Made

1. **Build Configuration**: Removed slick module references (deleted directory)
2. **CI/CD**: GitHub Actions with matrix build for all modules
3. **Publishing**: GitHub Packages as artifact repository
4. **Error Handling**: Standardize on Either for all modules (to be implemented)
5. **Interface Design**: Trait-based interfaces with concrete implementations

### Decisions Pending

1. **Versioning**: Independent versioning per module vs. unified versioning?
2. **Scala Version**: Continue with 2.13.17 or plan upgrade to Scala 3?
3. **Dependencies**: Plan for replacing Akka (note in Dependencies.scala)?
4. **Test Framework**: Continue with ScalaTest or consider alternatives?

---

## Questions for Review

1. **Module Versioning**: Should each module have independent version numbers?
2. **Breaking Changes**: Are breaking changes acceptable for Phase 2 refactoring?
3. **Priority Order**: Is the proposed order of implementation acceptable?
4. **Resource Allocation**: Is the time estimate reasonable?
5. **Scala 3 Migration**: Should we plan for Scala 3 migration?
6. **Akka Replacement**: Timeline for replacing Akka dependencies?

---

## Next Steps

### Immediate (Today)
1. ✅ Review this action plan
2. ✅ Review codebase review document
3. ⏳ Stage and commit changes
4. ⏳ Push to trigger CI/CD

### This Week
1. Verify compilation
2. Review CI/CD results
3. Plan Phase 1 work
4. Assign resources

### Next Week
1. Begin Phase 1 implementation
2. Set up testing infrastructure
3. Start writing tests

---

## Appendix: File Summary

### Created Files

1. **CODEBASE_REVIEW.md**
   - Comprehensive review of all modules
   - Interface design analysis
   - Testability assessment
   - Recommendations with priorities

2. **REFACTORING_EXAMPLES.md**
   - Concrete refactoring examples
   - PubSub module refactoring
   - SMTP module refactoring
   - Configuration pattern standardization
   - Redis serialization refactoring

3. **ACTION_PLAN.md** (this file)
   - Prioritized action items
   - Time estimates
   - Success metrics
   - Risk assessment

4. **.github/workflows/ci-cd.yml**
   - GitHub Actions workflow
   - Build and test job
   - Publish job with matrix strategy

### Modified Files

1. **build.sbt**
   - Removed slick module definition
   - Removed slick from aggregation

---

## Contact & Support

For questions or clarifications on this action plan:
- Review the CODEBASE_REVIEW.md for detailed analysis
- Review the REFACTORING_EXAMPLES.md for implementation patterns
- Consult with the development team

**Document Version**: 1.0  
**Last Updated**: November 10, 2025

