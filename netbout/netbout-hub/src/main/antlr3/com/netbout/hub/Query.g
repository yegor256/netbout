/**
 * @version $Id$
 */
grammar Query;

@header {
    package com.netbout.hub;
    import com.netbout.hub.predicates.NumberPred;
    import com.netbout.hub.predicates.TextPred;
    import com.netbout.hub.predicates.VariablePred;
    import java.util.List;
}

@lexer::header {
    package com.netbout.hub;
}

@lexer::members {
    @Override
    public void emitErrorMessage(String msg) {
        throw new PredicateException(msg);
    }
}

@parser::members {
    private transient PredicateBuilder builder;
    @Override
    public void emitErrorMessage(String msg) {
        throw new PredicateException(msg);
    }
    public void setPredicateBuilder(final PredicateBuilder bldr) {
        this.builder = bldr;
    }
}

query returns [Predicate ret]
    :
    predicate
    { $ret = $predicate.ret; }
    EOF
    ;

predicate returns [Predicate ret]
    @init { final List<Predicate> atoms = new ArrayList<Predicate>(); }
    :
    '('
    NAME
    (
        atom
        { atoms.add($atom.ret); }
    )*
    ')'
    { $ret = this.builder.build($NAME.text, atoms); }
    ;

atom returns [Predicate ret]
    :
    predicate
    { $ret = $predicate.ret; }
    |
    VARIABLE
    { $ret = new VariablePred($VARIABLE.text); }
    |
    TEXT
    { $ret = new TextPred($TEXT.text); }
    |
    NUMBER
    { $ret = new NumberPred(Long.valueOf($NUMBER.text)); }
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
