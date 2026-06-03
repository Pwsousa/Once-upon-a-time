package engine.core;

/**
 * Configuracao imutavel da janela. Use WindowConfig.Builder para construir.
 *
 * Exemplo:
 *   WindowConfig cfg = new WindowConfig.Builder("Meu Jogo")
 *       .width(1920).height(1080).vsync(true).build();
 */
public final class WindowConfig {

    public final int width;
    public final int height;
    public final String title;
    public final boolean vsync;
    public final boolean resizable;

    private WindowConfig(Builder b) {
        this.width     = b.width;
        this.height    = b.height;
        this.title     = b.title;
        this.vsync     = b.vsync;
        this.resizable = b.resizable;
    }

    public static final class Builder {
        private int width     = 1280;
        private int height    = 720;
        private boolean vsync     = true;
        private boolean resizable = true;
        private final String title;

        public Builder(String title) {
            if (title == null || title.isBlank()) throw new IllegalArgumentException("title obrigatorio");
            this.title = title;
        }

        public Builder width(int v)      { this.width     = v; return this; }
        public Builder height(int v)     { this.height    = v; return this; }
        public Builder vsync(boolean v)  { this.vsync     = v; return this; }
        public Builder resizable(boolean v) { this.resizable = v; return this; }

        public WindowConfig build() {
            if (width <= 0 || height <= 0) throw new IllegalStateException("largura/altura devem ser positivos");
            return new WindowConfig(this);
        }
    }
}
