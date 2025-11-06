package io.github.lunbun.quasar.client.glsim;

public final class GlNames {
    private GlNames() { }

    private static Object[] names = new Object[1024];
    private static final Object CLAIMED = new Object();

    public static int genName() {
        for (int i = 1; i < names.length; ++i) {
            if (names[i] == null) {
                names[i] = CLAIMED;
                return i;
            }
        }

        Object[] newNames = new Object[names.length * 2];
        System.arraycopy(names, 0, newNames, 0, names.length);
        int index = names.length;
        newNames[index] = CLAIMED;
        names = newNames;
        return index;
    }

    public static void deleteName(int i) {
        names[i] = null;
    }

    public static void setValue(int i, Object value) {
        names[i] = value;
    }

    public static Object getValue(int i) {
        return names[i];
    }
}
