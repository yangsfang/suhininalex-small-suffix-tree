package com.suhininalex.suffixtree

import java.util.*

internal val alpha: Double = 0.58

abstract class ScapeGoatNode<K, V>{
  internal var left: ScapeGoatNode<K, V>? = null
  internal var right: ScapeGoatNode<K, V>? = null
  internal abstract val key: K
  internal abstract val value: V
}

internal fun <K, V> ScapeGoatNode<K, V>.getOrSetLeft(element: ScapeGoatNode<K, V>): ScapeGoatNode<K, V> {
  if (left == null) {
    left = element
  }
  return left!!
}

internal fun h_alpha(alpha: Double, size: Int): Int {
  return -log(value = size.toDouble(), base = alpha).toInt()
}

internal fun <K, V> ScapeGoatNode<K, V>.getOrSetRight(element: ScapeGoatNode<K, V>): ScapeGoatNode<K, V> {
  if (right == null) {
    right = element
  }
  return right!!
}

internal tailrec fun <K, V> ScapeGoatNode<K, V>?.search(key: K): ScapeGoatNode<K, V>? {
  if (this == null) return null
  val cmp = comparator.compare(key, this.key)
  if (cmp > 0) return right.search(key)
  else if (cmp < 0) return left.search(key)
  else return this
}

internal fun <K, V> findScapeGoat(parents: Stack<ScapeGoatNode<K, V>>, node: ScapeGoatNode<K, V>): Pair<ScapeGoatNode<K, V>, ScapeGoatNode<K, V>?> {
  var n = node
  var size = 1
  var height = 0
  while (parents.isNotEmpty()){
    val parent = parents.pop()
    height += 1
    val totalSize = 1 + size + siblingOf(parent, n).size()
    val h_alpha = h_alpha(alpha, totalSize)
    if (height > h_alpha) return parent to parents.lastOrNull()
    n = parent
    size = totalSize
  }
  throw IllegalStateException("There is no scapegoat among parents.")
}

tailrec internal fun <K, V> findMinimum(node: ScapeGoatNode<K, V>, nodeParent: ScapeGoatNode<K, V>?): Pair<ScapeGoatNode<K, V>, ScapeGoatNode<K, V>?> {
  nodeParent?.checkIsChild(node)
  if (node.left != null)
    return findMinimum(node.left!!, node)
  else return Pair(node, nodeParent)
}

internal fun <K, V> ScapeGoatNode<K, V>.hasBothChildren() =
  left != null && right != null

internal fun <K, V> ScapeGoatNode<K, V>.checkIsChild(child: ScapeGoatNode<K, V>?){
  if (left !== child && right !== child)
    throw IllegalArgumentException("Parent: $key ${left?.key} ${right?.key} Child: ${child?.key}")
}

internal fun <K, V> ScapeGoatNode<K, V>?.children(accumulator: MutableList<ScapeGoatNode<K, V>>): Collection<ScapeGoatNode<K, V>>{
  if (this != null){
    accumulator.add(this)
    left.children(accumulator)
    right.children(accumulator)
  }
  return accumulator
}

internal fun <K, V> siblingOf(parent: ScapeGoatNode<K, V>, node: ScapeGoatNode<K, V>): ScapeGoatNode<K, V>? =
  if (parent.left === node)  parent.right
  else if (parent.right === node)  parent.left
  else throw IllegalArgumentException("Node $node must be a child of parent.")

internal fun <K, V> ScapeGoatNode<K, V>?.size(): Int =
  if (this != null) left.size() + right.size() + 1
  else 0

internal fun <K, V> flatten_tree(root: ScapeGoatNode<K, V>?, tail: ScapeGoatNode<K, V>? = null): ScapeGoatNode<K, V>? {
  if (root == null) return tail
  root.right = flatten_tree(root.right, tail)
  val result = flatten_tree(root.left, root)
  root.left = null
  return result
}

internal fun <K, V> Build_Height_Balanced_Tree(size: Int, head: ScapeGoatNode<K, V>?): Pair<ScapeGoatNode<K, V>?, ScapeGoatNode<K, V>?> {
  if (size == 1) {
    return Pair(head, head)
  }
  if (size == 2) {
    val root = head!!.right!!
    root.left = head
    head.right = null
    return Pair(root, root)
  }
  val (leftRoot, leftLast) = Build_Height_Balanced_Tree(size/2, head)
  val root = leftLast?.right ?: head

  root?.left = leftRoot

  val (rightRoot, rightLast) = Build_Height_Balanced_Tree(size - size/2 - 1, root?.right)
  root?.right = rightRoot

  leftLast?.right = null
  return Pair(root, rightLast)
}

internal fun <K, V> Rebuild_Tree(size: Int, scapegoat: ScapeGoatNode<K, V>): Pair<ScapeGoatNode<K, V>?, ScapeGoatNode<K, V>?> {
  val head = flatten_tree(scapegoat, null)!!
  val result = Build_Height_Balanced_Tree(size, head)
  return result
}

private fun log(value: Double, base: Double): Double =
  Math.log(value)/Math.log(base)
