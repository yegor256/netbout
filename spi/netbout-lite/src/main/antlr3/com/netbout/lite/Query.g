/**
 * @version $Id: Query.g 3116 2012-08-02 18:28:36Z guard $
 */
grammar Query;

@header {
    package com.netbout.lite;
    import com.netbout.lite.terms.AbstractTerm;
    import java.util.LinkedList;
    import java.util.List;
}

@lexer::header {
    package com.netbout.lite;
}

@lexer::members {
    @Override
    public void emitErrorMessage(String msg) {
        throw new IllegalArgumentException(msg);
    }
}

@parser::members {
    @Override
    public void emitErrorMessage(String msg) {
        throw new IllegalArgumentException(msg);
    }
}

term returns [Term ret]
    :
    composite
    { $ret = $composite.ret; }
    |
    atom
    { $ret = $atom.ret; }
    ;

composite returns [Term ret]
    @init { final List<Term> args = new LinkedList<Term>(); }
    :
    '('
    NAME
    (
        term
        { args.add($term.ret); }
    )*
    ')'
    { $ret = AbstractTerm.create($NAME.text, args); }
    ;

atom returns [Term ret]
    :
    VARIABLE
    { $ret = AbstractTerm.var($VARIABLE.text); }
    |
    TEXT
    { $ret = AbstractTerm.text($TEXT.text); }
    |
    NUMBER
    { $ret = AbstractTerm.text($NUMBER.text); }
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
