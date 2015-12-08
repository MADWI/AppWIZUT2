package pl.edu.zut.mad.appwizut2.utils;

/**
 * @author Damian Malarczyk
 */
public class Interfaces {
    public interface Equatable<T> {
        boolean compares(T another);
    }
    public interface CompletitionCallback {
        void finished(Boolean success);
    }
}


