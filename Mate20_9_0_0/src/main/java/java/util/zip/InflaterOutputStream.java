package java.util.zip;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class InflaterOutputStream extends FilterOutputStream {
    protected final byte[] buf;
    private boolean closed;
    protected final Inflater inf;
    private boolean usesDefaultInflater;
    private final byte[] wbuf;

    private void ensureOpen() throws IOException {
        if (this.closed) {
            throw new IOException("Stream closed");
        }
    }

    public InflaterOutputStream(OutputStream out) {
        this(out, new Inflater());
        this.usesDefaultInflater = true;
    }

    public InflaterOutputStream(OutputStream out, Inflater infl) {
        this(out, infl, 512);
    }

    public InflaterOutputStream(OutputStream out, Inflater infl, int bufLen) {
        super(out);
        this.wbuf = new byte[1];
        this.usesDefaultInflater = false;
        this.closed = false;
        if (out == null) {
            throw new NullPointerException("Null output");
        } else if (infl == null) {
            throw new NullPointerException("Null inflater");
        } else if (bufLen > 0) {
            this.inf = infl;
            this.buf = new byte[bufLen];
        } else {
            throw new IllegalArgumentException("Buffer size < 1");
        }
    }

    public void close() throws IOException {
        if (!this.closed) {
            try {
                finish();
            } finally {
                this.out.close();
                this.closed = true;
            }
        }
    }

    public void flush() throws IOException {
        ensureOpen();
        if (!this.inf.finished()) {
            while (!this.inf.finished() && !this.inf.needsInput()) {
                try {
                    int n = this.inf.inflate(this.buf, 0, this.buf.length);
                    if (n < 1) {
                        break;
                    }
                    this.out.write(this.buf, 0, n);
                } catch (DataFormatException ex) {
                    String msg = ex.getMessage();
                    if (msg == null) {
                        msg = "Invalid ZLIB data format";
                    }
                    throw new ZipException(msg);
                }
            }
            super.flush();
        }
    }

    public void finish() throws IOException {
        ensureOpen();
        flush();
        if (this.usesDefaultInflater) {
            this.inf.end();
        }
    }

    public void write(int b) throws IOException {
        this.wbuf[0] = (byte) b;
        write(this.wbuf, 0, 1);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if (b == null) {
            throw new NullPointerException("Null buffer for read");
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len != 0) {
            while (true) {
                try {
                    int part;
                    if (this.inf.needsInput()) {
                        if (len < 1) {
                            break;
                        }
                        part = 512;
                        if (len < 512) {
                            part = len;
                        }
                        this.inf.setInput(b, off, part);
                        off += part;
                        len -= part;
                    }
                    do {
                        part = this.inf.inflate(this.buf, 0, this.buf.length);
                        if (part > 0) {
                            this.out.write(this.buf, 0, part);
                            continue;
                        }
                    } while (part > 0);
                    if (this.inf.finished()) {
                        break;
                    } else if (this.inf.needsDictionary()) {
                        throw new ZipException("ZLIB dictionary missing");
                    }
                } catch (DataFormatException ex) {
                    String msg = ex.getMessage();
                    if (msg == null) {
                        msg = "Invalid ZLIB data format";
                    }
                    throw new ZipException(msg);
                }
            }
        }
    }
}
