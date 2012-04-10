/**
 * @version $Id$
 */
grammar Query;

@header {
    package com.netbout.inf;
    import com.netbout.inf.atoms.NumberAtom;
    import com.netbout.inf.atoms.TextAtom;
    import com.netbout.inf.atoms.VariableAtom;
    import java.util.LinkedList;
    import java.util.List;
}

@lexer::header {
    package com.netbout.inf;
}

@lexer::members {
    @Override
    public void emitErrorMessage(String msg) {
        throw new PredicateException(msg);
    }
}

@parser::members {
    private transient Store store;
    @Override
    public void emitErrorMessage(String msg) {
        throw new PredicateException(msg);
    }
    public void setStore(final Store str) {
        this.store = str;
    }
}

query returns [Predicate ret]
    :
    predicate
    { $ret = $predicate.ret; }
    EOF
    ;

predicate returns [Predicate ret]
    @init { final List<Atom> atoms = new LinkedList<Atom>(); }
    :
    '('
    NAME
    (
        atom
        { atoms.add($atom.ret); }
    )*
    ')'
    { $ret = this.store.build($NAME.text, atoms); }
    ;

atom returns [Atom ret]
    :
    predicate
    { $ret = $predicate.ret; }
    |
    VARIABLE
    { $ret = new VariableAtom($VARIABLE.text); }
    |
    TEXT
    { $ret = new TextAtom($TEXT.text); }
    |
    NUMBER
    { $ret = new NumberAtom(Long.valueOf($NUMBER.text)); }
    ;

NAME:
    LETTER ( LETTER | '-' )*
    |
    'urn:' ( LETTER | '-' )+ ':' ( LETTER | '-' | ':' | DIGIT )+
    ;

VARIABLE :
    '$' LETTER ( LETTER | '.' )+
    { this.setText(getText().substring(1)); }
    ;

TEXT :
    '"' ('\\"' | ~'"')* '"'
    { this.setText(this.getText().substring(1, this.getText().length() - 1).replace("\\\"", "\"")); }
    |
    '\'' ('\\\'' | ~'\'')* '\''
    { this.setText(this.getText().substring(1, this.getText().length() - 1).replace("\\'", "'")); }
    ;

NUMBER:
    DIGIT+
    ;

fragment LETTER:
    ( 'a' .. 'z' )
    ;
fragment DIGIT:
    ( '0' .. '9' )
    ;

SPACE
    :
    ( ' ' | '\t' | '\n' | '\r' )+
    { skip(); }
    ;
