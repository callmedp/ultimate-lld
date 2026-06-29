package com.ultimatelld.problems.urlshortener.core;

/** OCP id source: sequence, random, or distributed (snowflake/Redis range) — swappable. */
public interface IdGenerator {
    String next();
}
