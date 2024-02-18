package com.suhininalex.suffixtree

import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

val comparator: Comparator<Any> = tokenComparator

open class ScapeGoatTree<K, V>{
    internal var root: ScapeGoatNode<K, V>? = null
    internal var size: Int = 0
    internal var maxSize: Int = 0
}

internal fun <K, V> ScapeGoatTree<K, V>.put(element: ScapeGoatNode<K, V>): Stack<ScapeGoatNode<K, V>> {
    size += 1
    maxSize = size
    if (root == null) {
        root = element
        return Stack()
    } else {
        var current = root!!
        val parents = Stack<ScapeGoatNode<K, V>>()
        while (current !== element) {
            parents.push(current)
            val cmp = comparator.compare(element.key, current.key)
            if (cmp < 0) current = current.getOrSetLeft(element)
            else if (cmp > 0) current = current.getOrSetRight(element)
            else throw IllegalArgumentException("Duplicate element $element with $current")
        }
       return parents
    }
}

internal fun <K, V> ScapeGoatTree<K, V>.delete(key: K) {
    var current = root
    var parent: ScapeGoatNode<K, V>? = null
    while (current != null) {
        val cmp = comparator.compare(key, current.key)
        if (cmp < 0) { parent = current; current = current.left }
        else if (cmp > 0) { parent = current; current = current.right }
        else {
            if (current.hasBothChildren()){
                current.checkIsChild(current.right)
                val (minNode, minParent) = findMinimum(current.right!!, current)
                minParent?.checkIsChild(minNode)
                deleteIfHasEmptyChild(minNode, minParent)
                minNode.right = current.right
                minNode.left = current.left
                replaceChild(parent, current, minNode)
            } else {
                deleteIfHasEmptyChild(current, parent)
            }
            return
        }
    }
}

internal fun <K, V> ScapeGoatTree<K, V>.remove(key: K): Unit {
    delete(key)
    if (size < alpha*maxSize) {
        if (root != null) {
            root = Rebuild_Tree(size, root!!).first
            maxSize = size
        }
    }
}

internal fun <K, V> ScapeGoatTree<K, V>.get(key: K): ScapeGoatNode<K, V>? {
    val r = root.search(key)
    return r
}

internal fun <K, V> ScapeGoatTree<K, V>.deleteIfHasEmptyChild(node: ScapeGoatNode<K, V>, parent: ScapeGoatNode<K, V>?){
    parent?.checkIsChild(node)
    if (node.hasBothChildren()) throw IllegalArgumentException("Has both children.")
    val newNode = node.right ?: node.left
    replaceChild(parent, node, newNode)
    size -= 1

}

internal fun <K, V> ScapeGoatTree<K, V>.replaceChild(parent: ScapeGoatNode<K, V>?, oldChild: ScapeGoatNode<K, V>, newChild: ScapeGoatNode<K, V>?){
    parent?.checkIsChild(oldChild)
    if (parent == null){
        root = newChild
    } else if (parent.left === oldChild){
        parent.left = newChild
    } else if (parent.right === oldChild){
        parent.right = newChild
    } else {
        throw IllegalArgumentException("OldChild: ${oldChild.key} New: ${newChild?.key} Parent: ${parent.key} ${parent.left?.key} ${parent.right?.key}")
    }
}

internal fun <K, V> ScapeGoatTree<K, V>.entries(): Collection<ScapeGoatNode<K, V>>{
    return root.children(ArrayList())
}

internal fun <K, V> ScapeGoatTree<K, V>.insert(node: ScapeGoatNode<K, V>){
    val parents = put(node)
    val height = parents.size
    val h_alpha = h_alpha(alpha, size)
    if (height > h_alpha){
        val (scapegoat, scapeGoatParent) = findScapeGoat(parents, node)
        val isLeft = scapeGoatParent?.left === scapegoat
        val (root, last) = Rebuild_Tree(scapegoat.size(), scapegoat)
        if (scapeGoatParent == null) this.root = root
        else if (isLeft) scapeGoatParent.left = root
        else scapeGoatParent.right = root
    }
}

fun <K, V> ScapeGoatTree<K, V>.first(): V? {
    return root?.value
}

fun <K, V> ScapeGoatTree<K, V>.isEmpty(): Boolean {
    return root == null
}

fun <K, V> ScapeGoatTree<K, V>.containsOne(): Boolean {
    return root != null && root?.left == null && root?.right == null
}