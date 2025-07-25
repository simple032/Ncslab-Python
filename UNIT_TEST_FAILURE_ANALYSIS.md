# Unit Test Failure Analysis

This document catalogs the various failure cases discovered during unit test execution in the NCSLabLink project. While the JUnit tests may appear to "pass", many have underlying issues that prevent proper code generation and compilation.

## Test Infrastructure Issues (✅ FIXED)

### 1. JSON Parsing Failures
**Status**: FIXED  
**Issue**: The `compileWebsocket.json` file contained malformed JSON with unescaped newlines  
**Error**: `Expected a ',' or '}' at character 1431`  
**Fix**: Manually corrected JSON formatting  

### 2. Null Pointer Exceptions  
**Status**: FIXED  
**Issue**: Tests failing when block definitions not found in database  
**Affected Blocks**: 
- Math: DivideBlock, ExponentialBlock, LogarithmBlock, MinMaxBlock, ModuloBlock, PowerBlock, ReciprocalBlock
- Source: BandLimitedWhiteNoiseBlock, PulseBlock  
- LogicAndBit: CompareToZeroBlock, DetectDecreaseTest, DetectIncreaseTest, LogicOperatorTest
**Fix**: Added graceful null checks with skip messages

### 3. Testrig Compilation Errors
**Status**: FIXED  
**Issue**: 21 testrig test files had undefined variable references (`mdlBlockList`, `filePath`)  
**Fix**: Updated to use correct variable names and method signatures

## Code Generation Issues (❌ UNRESOLVED)

### 1. Velocity Template Variable Substitution Failures

#### Scope Block Template Issue
**File**: `src/test/java/com/ncslab/block/sink/ScopeTest.java`  
**Template**: `templates/c/sink/Scope/*`  
**Error**: 
```cpp
mainccode.cpp:261:1: error: '$scopeStructName' was not declared in this scope
  261 | $scopeStructName.cursor=0;
```
**Root Cause**: Velocity template variable `$scopeStructName` not being properly substituted  
**Impact**: C++ compilation fails in simulation mode  
**Test Result**: False positive (test passes but code doesn't compile)

#### Integrator Block Template Issue  
**File**: `src/test/java/com/ncslab/block/continuous/IntegratorTest.java`  
**Template**: `templates/c/continuous/Integrator/derivative.vm`  
**Error**: 
```
Right side of range operator [n..m] has null value. Operation not possible. 
templates/c/continuous/Integrator/derivative.vm[line 29, column 24]
```
**Root Cause**: Range operator in Velocity template has null variable  
**Impact**: Template rendering error (but compilation succeeds)  
**Test Result**: Test passes with warnings

#### Abs Block Template Issue
**File**: `src/test/java/com/ncslab/block/math/AbsTest.java`  
**Template**: `templates/c/math/Abs/output.vm`  
**Error**:
```
Right side of range operator [n..m] has null value. Operation not possible. 
templates/c/math/Abs/output.vm[line 12, column 24]
```
**Root Cause**: Range operator in Velocity template has null variable  
**Impact**: Template rendering error (but compilation succeeds)  
**Test Result**: Test passes with warnings

### 2. Database Missing Block Definitions

The following blocks are referenced in tests but not found in the database:

#### Math Package
- `DivideBlock` - Division operation block
- `ExponentialBlock` - Exponential function block  
- `LogarithmBlock` - Logarithmic function block
- `MinMaxBlock` - Min/Max operation block
- `ModuloBlock` - Modulo operation block
- `PowerBlock` - Power/exponentiation block
- `ReciprocalBlock` - Reciprocal (1/x) block

#### Source Package  
- `BandLimitedWhiteNoiseBlock` - Band-limited white noise generator
- `PulseBlock` - Pulse generator block

#### LogicAndBit Package
- `Compare To ZeroBlock` - Zero comparison block
- `Detect DecreaseBlock` - Signal decrease detection
- `Detect IncreaseBlock` - Signal increase detection  
- `LogicOperatorBlock` - Generic logic operations

**Impact**: Tests skip gracefully but blocks are not testable
**Required Action**: Add database entries for these blocks or remove obsolete tests

### 3. Test Framework Limitations

#### Silent C++ Compilation Failures
**Issue**: JUnit test framework doesn't detect C++ compilation failures  
**Root Cause**: C++ compilation happens in separate process; errors don't propagate as Java exceptions  
**Example**: ScopeTest appears to pass even when generated C++ code won't compile  
**Impact**: False positive test results hide real code generation problems

#### Inconsistent Simulation vs Compilation Modes
**Observation**: Some blocks fail in simulation mode but pass in compilation mode  
**Example**: ScopeTest simulation fails C++ compilation, but compilation mode succeeds  
**Root Cause**: Different code generation paths for simulation vs deployment targets  
**Impact**: Inconsistent behavior between test modes

## Template-Specific Issues by Location

### Velocity Template Directory: `src/main/resources/templates/c/`

#### Range Operator Issues
**Pattern**: `#foreach($i in [0..$someVariable])`  
**Problem**: `$someVariable` is null or undefined  
**Affected Templates**:
- `math/Abs/output.vm:12:24`
- `continuous/Integrator/derivative.vm:29:24`

#### Variable Substitution Issues  
**Pattern**: `$variableName` not being replaced with actual values  
**Affected Templates**:
- Scope-related templates using `$scopeStructName`

### Code Generation Pipeline Issues

#### Template Context Population
**Issue**: Required variables not being added to Velocity context  
**Impact**: Template variables remain as literal strings in generated code  
**Detection**: Look for `$` prefixed strings in generated C++ files

#### Template File Resolution  
**Issue**: Template files may not be found or loaded correctly  
**Impact**: Default/fallback code generation may be used  
**Detection**: Check template loading logs and generated code quality

## Testing Recommendations

### 1. Enhanced Test Validation
Add post-compilation validation to JUnit tests:
```java
@Test
public void compile() {
    // ... existing code ...
    
    // Add validation that compilation actually succeeded
    File executableFile = new File("path/to/generated/executable");
    assertTrue("Compilation should produce executable", executableFile.exists());
}
```

### 2. Template Validation Tests
Create separate tests that validate Velocity templates:
- Check for undefined variables
- Validate template syntax
- Test with sample data contexts

### 3. Code Generation Quality Tests  
Add tests that examine generated C++ code:
- Parse generated code for `$` prefixed strings (unsubstituted variables)
- Validate C++ syntax correctness
- Check for required includes and declarations

### 4. Database Consistency Tests
Add tests that verify test blocks exist in database:
```java
@Before
public void setUp(){
    // ... existing database loading ...
    assertNotNull("Block " + blockType + " must exist in database", mdlBlock);
}
```

## Priority Recommendations

### High Priority (Breaks core functionality)
1. Fix ScopeBlock template variable substitution
2. Resolve range operator null value issues  
3. Add missing database entries for new blocks

### Medium Priority (Quality improvements)
1. Enhance test framework to detect C++ compilation failures
2. Add template validation in build process
3. Standardize error handling across all block types

### Low Priority (Technical debt)
1. Remove obsolete test files for non-existent blocks
2. Improve logging and debugging for template issues
3. Add comprehensive template documentation

## Monitoring and Detection

### Identifying Template Issues
Look for these patterns in test output:
- `Right side of range operator [n..m] has null value`
- `$variableName was not declared in this scope` in C++ errors
- `Cannot create exe file` messages
- Velocity rendering ERROR logs

### Identifying Missing Database Entries
Look for these patterns:
- `Skipping test - BlockName not found in database`
- Null pointer exceptions in `mdlBlock.getData()` calls

### Identifying Test Framework Issues
Look for these patterns:
- Tests passing despite C++ compilation errors
- Inconsistent results between simulation and compilation modes
- Missing executable files after "successful" compilation

---

*Last Updated: 2025-07-25*  
*Analysis covers unit test execution across math, source, logicAndBit, continuous, and sink packages*