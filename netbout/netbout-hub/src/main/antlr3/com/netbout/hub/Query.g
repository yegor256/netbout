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
        throw new IllegalArgumentException(msg);
    }
}

@parser::members {
    @Override
    public void emitErrorMessage(String msg) {
        throw new IllegalArgumentException(msg);
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
    { $ret = new PredicateBuilder().build($NAME.text, atoms); }
    ;
    catch [PredicateException ex] {
        throw new RecognitionException();
    }

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

NAME: ( 'a' .. 'z' | '-' )*;
VARIABLE : '$' ( 'a' .. 'z' )+
    { setText(getText().substring(1)); }
    ;
TEXT : '"' ('\\"' | ~'"')* '"'
    { setText(getText().substring(1, getText().length() - 1).replace("\\\"", "\"")); }
    ;
NUMBER: ( '0' .. '9' )+;
SPACE
    :
    ( ' ' | '\t' | '\n' | '\r' )+
    { skip(); }
    ;
