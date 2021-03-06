package sun.invoke.util;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.Arrays;

public enum Wrapper {
    BOOLEAN(Boolean.class, Boolean.TYPE, 'Z', Boolean.valueOf(false), new boolean[0], Format.unsigned(1)),
    BYTE(Byte.class, Byte.TYPE, 'B', Byte.valueOf((byte) 0), new byte[0], Format.signed(8)),
    SHORT(Short.class, Short.TYPE, 'S', Short.valueOf((short) 0), new short[0], Format.signed(16)),
    CHAR(Character.class, Character.TYPE, 'C', Character.valueOf(0), new char[0], Format.unsigned(16)),
    INT(Integer.class, Integer.TYPE, 'I', Integer.valueOf(0), new int[0], Format.signed(32)),
    LONG(Long.class, Long.TYPE, 'J', Long.valueOf(0), new long[0], Format.signed(64)),
    FLOAT(Float.class, Float.TYPE, 'F', Float.valueOf(0.0f), new float[0], Format.floating(32)),
    DOUBLE(Double.class, Double.TYPE, 'D', Double.valueOf(0.0d), new double[0], Format.floating(64)),
    OBJECT(Object.class, Object.class, 'L', null, new Object[0], Format.other(1)),
    VOID(Void.class, Void.TYPE, 'V', null, null, Format.other(0));
    
    private static final Wrapper[] FROM_CHAR = null;
    private static final Wrapper[] FROM_PRIM = null;
    private static final Wrapper[] FROM_WRAP = null;
    private final char basicTypeChar;
    private final Object emptyArray;
    private final int format;
    private final String primitiveSimpleName;
    private final Class<?> primitiveType;
    private final String wrapperSimpleName;
    private final Class<?> wrapperType;
    private final Object zero;

    private static abstract class Format {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        static final int BOOLEAN = 5;
        static final int CHAR = 65;
        static final int FLOAT = 4225;
        static final int FLOATING = 4096;
        static final int INT = -3967;
        static final int KIND_SHIFT = 12;
        static final int NUM_MASK = -4;
        static final int SHORT = -4031;
        static final int SIGNED = -4096;
        static final int SIZE_MASK = 1023;
        static final int SIZE_SHIFT = 2;
        static final int SLOT_MASK = 3;
        static final int SLOT_SHIFT = 0;
        static final int UNSIGNED = 0;
        static final int VOID = 0;

        static {
            Class cls = Wrapper.class;
        }

        private Format() {
        }

        static int format(int kind, int size, int slots) {
            return ((size << 2) | kind) | (slots << 0);
        }

        static int signed(int size) {
            return format(SIGNED, size, size > 32 ? 2 : 1);
        }

        static int unsigned(int size) {
            return format(0, size, size > 32 ? 2 : 1);
        }

        static int floating(int size) {
            return format(4096, size, size > 32 ? 2 : 1);
        }

        static int other(int slots) {
            return slots << 0;
        }
    }

    static {
        FROM_PRIM = new Wrapper[16];
        FROM_WRAP = new Wrapper[16];
        FROM_CHAR = new Wrapper[16];
        Wrapper[] values = values();
        int length = values.length;
        int i;
        while (i < length) {
            Wrapper w = values[i];
            int pi = hashPrim(w.primitiveType);
            int wi = hashWrap(w.wrapperType);
            int ci = hashChar(w.basicTypeChar);
            FROM_PRIM[pi] = w;
            FROM_WRAP[wi] = w;
            FROM_CHAR[ci] = w;
            i++;
        }
    }

    private Wrapper(Class<?> wtype, Class<?> ptype, char tchar, Object zero, Object emptyArray, int format) {
        this.wrapperType = wtype;
        this.primitiveType = ptype;
        this.basicTypeChar = tchar;
        this.zero = zero;
        this.emptyArray = emptyArray;
        this.format = format;
        this.wrapperSimpleName = wtype.getSimpleName();
        this.primitiveSimpleName = ptype.getSimpleName();
    }

    public String detailString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.wrapperSimpleName);
        Object[] objArr = new Object[5];
        objArr[0] = this.wrapperType;
        objArr[1] = this.primitiveType;
        objArr[2] = Character.valueOf(this.basicTypeChar);
        objArr[3] = this.zero;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("0x");
        stringBuilder2.append(Integer.toHexString(this.format));
        objArr[4] = stringBuilder2.toString();
        stringBuilder.append(Arrays.asList(objArr));
        return stringBuilder.toString();
    }

    public int bitWidth() {
        return (this.format >> 2) & 1023;
    }

    public int stackSlots() {
        return (this.format >> 0) & 3;
    }

    public boolean isSingleWord() {
        return (this.format & 1) != 0;
    }

    public boolean isDoubleWord() {
        return (this.format & 2) != 0;
    }

    public boolean isNumeric() {
        return (this.format & -4) != 0;
    }

    public boolean isIntegral() {
        return isNumeric() && this.format < 4225;
    }

    public boolean isSubwordOrInt() {
        return isIntegral() && isSingleWord();
    }

    public boolean isSigned() {
        return this.format < 0;
    }

    public boolean isUnsigned() {
        return this.format >= 5 && this.format < 4225;
    }

    public boolean isFloating() {
        return this.format >= 4225;
    }

    public boolean isOther() {
        return (this.format & -4) == 0;
    }

    public boolean isConvertibleFrom(Wrapper source) {
        if (this == source) {
            return true;
        }
        if (compareTo((Enum) source) < 0) {
            return false;
        }
        if ((((this.format & source.format) & -4096) != 0) || isOther() || source.format == 65) {
            return true;
        }
        return false;
    }

    private static boolean checkConvertibleFrom() {
        for (Wrapper w : values()) {
            Wrapper[] values;
            int length;
            int i;
            Wrapper x;
            if (w != VOID) {
            }
            if (w == CHAR || !w.isConvertibleFrom(INT)) {
            }
            if (w == BOOLEAN || w == VOID || w != OBJECT) {
            }
            if (w.isSigned()) {
                values = values();
                length = values.length;
                i = 0;
                while (i < length) {
                    x = values[i];
                    i = (w != x && !x.isFloating() && x.isSigned() && w.compareTo((Enum) x) < 0) ? i + 1 : i + 1;
                }
            }
            if (w.isFloating()) {
                values = values();
                length = values.length;
                i = 0;
                while (i < length) {
                    x = values[i];
                    i = (w != x && !x.isSigned() && x.isFloating() && w.compareTo((Enum) x) < 0) ? i + 1 : i + 1;
                }
            }
        }
        return true;
    }

    public Object zero() {
        return this.zero;
    }

    public <T> T zero(Class<T> type) {
        return convert(this.zero, type);
    }

    public static Wrapper forPrimitiveType(Class<?> type) {
        Wrapper w = findPrimitiveType(type);
        if (w != null) {
            return w;
        }
        if (type.isPrimitive()) {
            throw new InternalError();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("not primitive: ");
        stringBuilder.append((Object) type);
        throw newIllegalArgumentException(stringBuilder.toString());
    }

    static Wrapper findPrimitiveType(Class<?> type) {
        Wrapper w = FROM_PRIM[hashPrim(type)];
        if (w == null || w.primitiveType != type) {
            return null;
        }
        return w;
    }

    public static Wrapper forWrapperType(Class<?> type) {
        Wrapper w = findWrapperType(type);
        if (w != null) {
            return w;
        }
        Wrapper[] values = values();
        int length = values.length;
        int i = 0;
        while (i < length) {
            if (values[i].wrapperType != type) {
                i++;
            } else {
                throw new InternalError();
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("not wrapper: ");
        stringBuilder.append((Object) type);
        throw newIllegalArgumentException(stringBuilder.toString());
    }

    static Wrapper findWrapperType(Class<?> type) {
        Wrapper w = FROM_WRAP[hashWrap(type)];
        if (w == null || w.wrapperType != type) {
            return null;
        }
        return w;
    }

    public static Wrapper forBasicType(char type) {
        Wrapper w = FROM_CHAR[hashChar(type)];
        if (w != null && w.basicTypeChar == type) {
            return w;
        }
        Wrapper[] values = values();
        int length = values.length;
        int i = 0;
        while (i < length) {
            Wrapper x = values[i];
            if (w.basicTypeChar != type) {
                i++;
            } else {
                throw new InternalError();
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("not basic type char: ");
        stringBuilder.append(type);
        throw newIllegalArgumentException(stringBuilder.toString());
    }

    public static Wrapper forBasicType(Class<?> type) {
        if (type.isPrimitive()) {
            return forPrimitiveType(type);
        }
        return OBJECT;
    }

    private static int hashPrim(Class<?> x) {
        String xn = x.getName();
        if (xn.length() < 3) {
            return 0;
        }
        return (xn.charAt(0) + xn.charAt(2)) % 16;
    }

    private static int hashWrap(Class<?> x) {
        String xn = x.getName();
        if (xn.length() < 13) {
            return 0;
        }
        return ((3 * xn.charAt(11)) + xn.charAt(12)) % 16;
    }

    private static int hashChar(char x) {
        return ((x >> 1) + x) % 16;
    }

    public Class<?> primitiveType() {
        return this.primitiveType;
    }

    public Class<?> wrapperType() {
        return this.wrapperType;
    }

    public <T> Class<T> wrapperType(Class<T> exampleType) {
        if (exampleType == this.wrapperType) {
            return exampleType;
        }
        if (exampleType == this.primitiveType || this.wrapperType == Object.class || exampleType.isInterface()) {
            return forceType(this.wrapperType, exampleType);
        }
        throw newClassCastException(exampleType, this.primitiveType);
    }

    private static ClassCastException newClassCastException(Class<?> actual, Class<?> expected) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append((Object) actual);
        stringBuilder.append(" is not compatible with ");
        stringBuilder.append((Object) expected);
        return new ClassCastException(stringBuilder.toString());
    }

    public static <T> Class<T> asWrapperType(Class<T> type) {
        if (type.isPrimitive()) {
            return forPrimitiveType(type).wrapperType(type);
        }
        return type;
    }

    public static <T> Class<T> asPrimitiveType(Class<T> type) {
        Wrapper w = findWrapperType(type);
        if (w != null) {
            return forceType(w.primitiveType(), type);
        }
        return type;
    }

    public static boolean isWrapperType(Class<?> type) {
        return findWrapperType(type) != null;
    }

    public static boolean isPrimitiveType(Class<?> type) {
        return type.isPrimitive();
    }

    public static char basicTypeChar(Class<?> type) {
        if (type.isPrimitive()) {
            return forPrimitiveType(type).basicTypeChar();
        }
        return 'L';
    }

    public char basicTypeChar() {
        return this.basicTypeChar;
    }

    public String wrapperSimpleName() {
        return this.wrapperSimpleName;
    }

    public String primitiveSimpleName() {
        return this.primitiveSimpleName;
    }

    public <T> T cast(Object x, Class<T> type) {
        return convert(x, type, true);
    }

    public <T> T convert(Object x, Class<T> type) {
        return convert(x, type, false);
    }

    private <T> T convert(Object x, Class<T> type, boolean isCast) {
        if (this == OBJECT) {
            if (!type.isInterface()) {
                type.cast(x);
            }
            return x;
        }
        Class<T> wtype = wrapperType(type);
        if (wtype.isInstance(x)) {
            return wtype.cast(x);
        }
        if (!isCast) {
            Class<?> sourceType = x.getClass();
            Wrapper source = findWrapperType(sourceType);
            if (source == null || !isConvertibleFrom(source)) {
                throw newClassCastException(wtype, sourceType);
            }
        } else if (x == null) {
            return this.zero;
        }
        return wrap(x);
    }

    static <T> Class<T> forceType(Class<?> type, Class<T> exampleType) {
        boolean z = type == exampleType || ((type.isPrimitive() && forPrimitiveType(type) == findWrapperType(exampleType)) || ((exampleType.isPrimitive() && forPrimitiveType(exampleType) == findWrapperType(type)) || (type == Object.class && !exampleType.isPrimitive())));
        if (!z) {
            PrintStream printStream = System.out;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append((Object) type);
            stringBuilder.append(" <= ");
            stringBuilder.append((Object) exampleType);
            printStream.println(stringBuilder.toString());
        }
        return type;
    }

    public Object wrap(Object x) {
        char c = this.basicTypeChar;
        if (c == 'L') {
            return x;
        }
        if (c == 'V') {
            return null;
        }
        Number xn = numberValue(x);
        char c2 = this.basicTypeChar;
        if (c2 == 'F') {
            return Float.valueOf(xn.floatValue());
        }
        if (c2 == 'S') {
            return Short.valueOf((short) xn.intValue());
        }
        if (c2 == 'Z') {
            return Boolean.valueOf(boolValue(xn.byteValue()));
        }
        switch (c2) {
            case 'B':
                return Byte.valueOf((byte) xn.intValue());
            case 'C':
                return Character.valueOf((char) xn.intValue());
            case 'D':
                return Double.valueOf(xn.doubleValue());
            default:
                switch (c2) {
                    case 'I':
                        return Integer.valueOf(xn.intValue());
                    case 'J':
                        return Long.valueOf(xn.longValue());
                    default:
                        throw new InternalError("bad wrapper");
                }
        }
    }

    public Object wrap(int x) {
        if (this.basicTypeChar == 'L') {
            return Integer.valueOf(x);
        }
        char c = this.basicTypeChar;
        if (c == 'F') {
            return Float.valueOf((float) x);
        }
        if (c == 'L') {
            throw newIllegalArgumentException("cannot wrap to object type");
        } else if (c == 'S') {
            return Short.valueOf((short) x);
        } else {
            if (c == 'V') {
                return null;
            }
            if (c == 'Z') {
                return Boolean.valueOf(boolValue((byte) x));
            }
            switch (c) {
                case 'B':
                    return Byte.valueOf((byte) x);
                case 'C':
                    return Character.valueOf((char) x);
                case 'D':
                    return Double.valueOf((double) x);
                default:
                    switch (c) {
                        case 'I':
                            return Integer.valueOf(x);
                        case 'J':
                            return Long.valueOf((long) x);
                        default:
                            throw new InternalError("bad wrapper");
                    }
            }
        }
    }

    private static Number numberValue(Object x) {
        if (x instanceof Number) {
            return (Number) x;
        }
        if (x instanceof Character) {
            return Integer.valueOf(((Character) x).charValue());
        }
        if (x instanceof Boolean) {
            return Integer.valueOf(((Boolean) x).booleanValue());
        }
        return (Number) x;
    }

    private static boolean boolValue(byte bits) {
        return ((byte) (bits & 1)) != (byte) 0;
    }

    private static RuntimeException newIllegalArgumentException(String message, Object x) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(message);
        stringBuilder.append(x);
        return newIllegalArgumentException(stringBuilder.toString());
    }

    private static RuntimeException newIllegalArgumentException(String message) {
        return new IllegalArgumentException(message);
    }

    public Object makeArray(int len) {
        return Array.newInstance(this.primitiveType, len);
    }

    public Class<?> arrayType() {
        return this.emptyArray.getClass();
    }

    public void copyArrayUnboxing(Object[] values, int vpos, Object a, int apos, int length) {
        if (a.getClass() != arrayType()) {
            arrayType().cast(a);
        }
        for (int i = 0; i < length; i++) {
            Array.set(a, i + apos, convert(values[i + vpos], this.primitiveType));
        }
    }

    public void copyArrayBoxing(Object a, int apos, Object[] values, int vpos, int length) {
        if (a.getClass() != arrayType()) {
            arrayType().cast(a);
        }
        for (int i = 0; i < length; i++) {
            values[i + vpos] = Array.get(a, i + apos);
        }
    }
}
