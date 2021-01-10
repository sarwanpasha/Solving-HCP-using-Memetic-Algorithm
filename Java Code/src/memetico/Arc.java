package memetico;/*
 *File: Arc.java
 *
 * Date      Authors
 * 8/22/99   Luciana Buriol and Pablo Moscato
 *
 */


//package MemePool.graph;

/**
 * A class for representing an arc of a directed graph.
 *
 * @author Luciana Buriol and Pablo Moscato
 **/

public class Arc {

    public int from, tip;       // in a previous version `Tip' was called `Next',
    // and `From' was called `Prev'.
    // `Tip' would have also been called `To'...
    // we keep the name `Tip' since it is the same in the
    //   Stanford Graph Base (SGB).


// double Len;       // `Len' stands for `length' in the SGB.
    //  For weighted graphs we can use it as the extra
    //  field we need to store the weight.


// int TipInNextArc; // The SGB uses a field called `next' to refer to
    // a pointer to an Arc (to have a linked list of
    // arcs outgoing from `From').

    public Arc() {
        from = -1;
        tip = -1;
    }

    /**
     * Copy constructor for Arc class.
     *
     * @param arc:Arc Arc to be copied to this instance.
     */
    public Arc(Arc arc) {
        from = arc.from;
        tip = arc.tip;
    }

}// end of class Arc
