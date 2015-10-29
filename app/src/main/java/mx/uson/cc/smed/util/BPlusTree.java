package mx.uson.cc.smed.util;


import java.util.TreeMap;

/**
 * Created by nancio on 29/10/15.
 */
public class BPlusTree<K extends Comparable<K>,V> {

    public static final int GRADE = 5;
    private static final int MIN = (GRADE-1)/2;

    Node root = null;
    int n = 0;

    public boolean insert(K key, V val){
        Node aux = root;
        while(aux != null){
            if(!aux.hasChildren()){
                if(aux.insert(key, val, null)){
                    rebalance(aux);
                    ++n;
                    return true;
                }else return false;
            }else {
                aux = aux.getSon(key);
                if(aux == null) return false;
            }
        }
        root = new Node<K,V>(key, val, null);
        ++n;
        return true;
    }

    public boolean remove(K key){
        Node aux = root;
        while(aux != null) {
            if (aux.hasChildren()) aux = aux.getSon(key);
            else if (aux.remove(key)) {
                rebalance(aux);
                --n;
                return true;
            } else return false;
        }
        return false; //TODO: throw shit
    }

    public V get(K key){
        Node actual = root;
        while(actual.hasChildren())
            actual = actual.getSon(key);
        for(int i=0; i<actual.n; ++i)
            if(actual.keys[i] == key) return (V)actual.vals[i];
        return null;
    }

    public void empty(){
        if(root == null) return;
        root.empty();
        root = null;
        n = 0;
    }

    public int size(){
        return n;
    }

    private void rebalance(Node r){
        if(r.n == GRADE){
            //>=MAX
            if(r==root){
                r.dad = new Node(r);
                root = r.dad;
            }
            r.split();
            if(r.dad != null) rebalance(r.dad);
        }else if (r.n < MIN){
            if(r == root){
                if(r.n == 0){
                    root = (Node)r.children[0];
                    //delete r;
                }
                return;
            }
            if(!r.pedirPrestado()){
                Node papi = r.dad;
                r.merge();
                rebalance(papi);
            }
        }
    }

    private static class Node<K extends Comparable<K>,V> {
        Object keys[] = new Object[GRADE];
        Object vals[] = new Object[GRADE];
        Object children[] = new Object[GRADE+1];
        Node dad;
        int n;

        public Node(K key, V val, Node<K,V> dad){
            this.keys[0] = key;
            this.vals[0] = val;
            this.dad = dad;
            n = 1;
        }

        public Node(Node child){
            children[0] = child;
            n = 0;
        }

        public void empty(){
            if(hasChildren()){
                for(int i=0; i<n; ++i)
                    ((Node)children[i]).empty();
            }
        }

        public boolean insert(K key, V val, Node child){
            K kaux;
            V vaux;
            Node naux;
            if(n == 0 || ((K)keys[n-1]).compareTo(key) == -1){
                vals[n++] = val;
                children[n] = child;
                return true;
            }
            for(int i=0; i<n; ++i){
                if(((K)keys[i]).compareTo(key) == 1){
                    vals[n] = vals[i];
                    vals[i] = val;
                    keys[n] = keys[i];
                    keys[i] = key;
                    children[n+1] = children[i+1];
                    children[i+1] = child;
                    for(int j=n++ -1; j>i; --j){
                        vaux = (V)vals[j];
                        vals[j] = vals[j+1];
                        vals[j+1] = vaux;
                        kaux = (K)keys[j];
                        keys[j] = keys[j+1];
                        keys[j+1] = kaux;
                        naux = (Node)children[j+1];
                        children[j+1] = children[j+2];
                        children[j+2] = naux;
                    }
                    break;
                }else if(((K)keys[i]).compareTo(key) == 0) return false;
            }
            return true;
        }

        public boolean remove(K key){
            int i=0;
            for (; i < n; ++i)
                if(((K)keys[i]).compareTo(key) == 0) break;
            if(i == n) return false;

            for (int j = i; j < n-1; ++j) {
                vals[j] = vals[j + 1];
                keys[j] = keys[j + 1];
            }
            --n;
            return true;
        }

        public void split(){
            int mitad = GRADE/2;
            Node der = new Node<K,V>((K)keys[mitad], (V)vals[mitad], dad);
            n = mitad;

            if(hasChildren()){
                der.n--;
                for(int i=mitad+1; i<GRADE; ++i){
                    der.vals[der.n] = vals[i];
                    der.keys[der.n] = keys[i];
                    ((Node)children[i]).dad = der;
                    der.children[der.n++] = children[i];
                }
                ((Node)children[GRADE]).dad = der;
                der.children[der.n] = children[GRADE];
            }else{
                for(int i=mitad+1; i<GRADE; ++i) {
                    der.keys[der.n] = keys[i];
                    der.vals[der.n++] = vals[i];
                }
                //lista enlazada b+
                der.children[0] = children[0];
                children[0] = der;
            }
            dad.insert((K) keys[mitad], (V) vals[mitad], der);

        }

        public void merge(){
            //buscar indice en padre
            Node hermano, aux = dad;

            int i=0;
            while(dad.children[i] != this) ++i;
            if(i==0){
                hermano = (Node)dad.children[i+1];

                if(hermano.hasChildren()){

                    keys[n] = dad.keys[i];
                    vals[n++] = dad.vals[i];
                    for(int j=0; j<hermano.n; ++j){
                        children[n] = hermano.children[j];
                        ((Node)children[n]).dad = this;
                        keys[n] = hermano.keys[j];
                        vals[n++] = hermano.vals[j];
                    }
                    children[n] = hermano.children[hermano.n];
                    ((Node)children[n]).dad = this;
                }else{
                    for(int j=0; j<hermano.n; ++j) {
                        keys[n] = hermano.keys[j];
                        vals[n++] = hermano.vals[j];
                    }
                    children[0] = hermano.children[0];
                }
                //delete hermano;
            }else{
                hermano = (Node)dad.children[i-1];

                if(hermano.hasChildren()){

                    hermano.keys[hermano.n] = dad.vals[i-1];
                    hermano.vals[hermano.n++] = dad.vals[i-1];
                    hermano.children[hermano.n] = children[0];
                    ((Node)hermano.children[hermano.n]).dad = hermano;
                    for(int j=0; j<n; ++j){
                        hermano.keys[hermano.n] = keys[j];
                        hermano.vals[hermano.n++] = vals[j];
                        hermano.children[hermano.n] = children[j+1];
                        ((Node)hermano.children[hermano.n]).dad = hermano;
                    }
                }else{
                    for(int j=0; j<n; ++j) {
                        hermano.keys[hermano.n] = keys[j];
                        hermano.vals[hermano.n++] = vals[j];
                    }
                    hermano.children[0] = children[0];
                }
                //delete this;
                --i;
            }
            for(; i<aux.n-1; ++i){
                aux.keys[i] = aux.keys[i+1];
                aux.vals[i] = aux.vals[i+1];
                aux.children[i+1] = aux.children[i+2];
            }
            --aux.n;
        }

        public boolean pedirPrestado(){
            Node hermano;
            int i=0;

            while(dad.children[i] != this) ++i;
            if(i==0 || (i!=dad.n && ((Node)dad.children[i+1]).n > ((Node)dad.children[i-1]).n)){
                hermano = (Node)dad.children[i+1];

                if(hermano.n == (GRADE-1)/2) return false;
                if(hermano.hasChildren()){

                    keys[n] = dad.keys[i];
                    vals[n++] = dad.vals[i];
                    children[n] = hermano.children[0];
                    ((Node)children[n]).dad = this;
                    dad.vals[i] = hermano.vals[0];
                    dad.keys[i] = hermano.keys[0];
                    for(int j=0; j<hermano.n -1; ++j){
                        hermano.keys[j] = hermano.keys[j+1];
                        hermano.vals[j] = hermano.vals[j+1];
                        hermano.children[j] = hermano.children[j+1];
                    }
                    hermano.children[hermano.n-1] = hermano.children[hermano.n];
                }else{
                    keys[n] = hermano.keys[0];
                    vals[n++] = hermano.vals[0];
                    dad.vals[i] = hermano.vals[1];
                    dad.keys[i] = hermano.keys[1];
                    for(int j=0; j<hermano.n -1; ++j) {
                        hermano.vals[j] = hermano.vals[j + 1];
                        hermano.keys[j] = hermano.keys[j + 1];
                    }
                }
                --hermano.n;
            }else{
                hermano = (Node)dad.children[i-1];

                if(hermano.n == (GRADE-1)/2) return false;
                if(hermano.hasChildren()){

                    children[n+1] = children[n];
                    for(int j=n++; j>0; --j){
                        vals[j] = vals[j-1];
                        keys[i] = keys[j-1];
                        children[j] = children[j-1];
                    }
                    vals[0] = dad.vals[i-1];
                    keys[0] = dad.keys[i-1];
                    children[0] = hermano.children[hermano.n];
                    ((Node)children[0]).dad = this;
                    dad.vals[i-1] = hermano.vals[--hermano.n];
                }else{
                    for(int j=n++; j>0; --j) {
                        vals[j] = vals[j - 1];
                        keys[j] = keys[j - 1];
                    }
                    vals[0] = hermano.vals[--hermano.n];
                    keys[0] = hermano.keys[hermano.n];
                    dad.vals[i-1] = vals[0];
                    dad.keys[i-1] = keys[0];
                }
            }
            return true;
        }

        public Node getSon(K valor){
            for(int i=0; i<n; ++i)
                if(valor.compareTo((K)keys[i]) < 0) return (Node)children[i];
            return (Node)children[n];
        }

        public boolean hasChildren(){
            return children[1] != null;
        }
    }

    private class LeafNode<K extends Comparable<K>,V> extends Node<K,V> {

        Object vals[] = new Object[GRADE];
        Node next = null;

        public LeafNode(K key, V val, Node<K, V> dad) {
            super(key, val, dad);
        }

        public LeafNode(Node child) {
            super(child);
        }
    }
}
