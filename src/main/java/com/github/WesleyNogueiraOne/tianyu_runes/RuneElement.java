package com.github.WesleyNogueiraOne.tianyu_runes;

public enum RuneElement {
    FIRE("fire"),
    WATER("water");
    // novo elemento = uma linha só, ex (Wu Xing): EARTH("earth"), METAL("metal"), WOOD("wood")...

    private final String id;
    RuneElement(String id) { this.id = id; }
    public String getId() { return id; }
}