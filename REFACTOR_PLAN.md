# Math Semantic Refactoring Plan

## Goal
Replace float arrays, FloatBuffers, and manual matrix/vector operations with JOML types for better performance, readability, and maintainability.

## Identified Candidates

### 1. SMath.java - Matrix and Vector Operations
**File:** `src/main/java/net/optifine/shaders/SMath.java`

Current state:
- `multiplyMat4xMat4(float[], float[], float[])` - Manual 4x4 matrix multiplication
- `multiplyMat4xVec4(float[], float[], float[])` - Manual matrix-vector multiplication  
- `invertMat4(float[], float[])` - Manual matrix inversion
- `invertMat4FBFA(FloatBuffer, FloatBuffer, float[], float[])` - FloatBuffer version

**Refactoring:** Replace with JOML Matrix4f operations

### 2. Shaders.java - Position Vectors
**File:** `src/main/java/net/optifine/shaders/Shaders.java`

Current state (lines 192-199):
- `float[] sunPosition = new float[4]`
- `float[] moonPosition = new float[4]`
- `float[] shadowLightPosition = new float[4]`
- `float[] upPosition = new float[4]`
- `float[] shadowLightPositionVector = new float[4]`
- `float[] upPosModelView = new float[]{0.0F, 100.0F, 0.0F, 0.0F}`
- `float[] sunPosModelView = new float[]{0.0F, 100.0F, 0.0F, 0.0F}`
- `float[] moonPosModelView = new float[]{0.0F, -100.0F, 0.0F, 0.0F}`

**Refactoring:** Replace with JOML Vector4f (already imported!)

### 3. ClippingHelper.java - Already Done! ✓
This has already been refactored to use JOML types (Matrix4f, FrustumIntersection).

## Implementation Steps

1. Refactor SMath.java to use JOML Matrix4f and Vector4f
2. Update Shaders.java to use Vector4f instead of float arrays
3. Update all call sites to use the new JOML-based methods
4. Test that everything compiles

## Benefits
- Cleaner, more readable code
- Better performance (JOML is optimized)
- Type safety
- Less manual array indexing errors
- Consistent with modern Java/OpenGL practices
