package com.example.concurrency.patterns.LockFreeQueue;

public class UnThreadSafeQueue<E> {
    //定义Node节点
    private static class Node<E> {
        private E element;//节点内存储的元素
        private Node<E> next;//下一个节点

        public Node(E element, Node<E> next) {
            super();
            this.element = element;
            this.next = next;
        }

        public E getElement() {
            return element;
        }

        public void setElement(E element) {
            this.element = element;
        }

        public Node<E> getNext() {
            return next;
        }

        public void setNext(Node<E> next) {
            this.next = next;
        }

        @Override
        public String toString() {
            return (element == null) ? "" : element.toString();
        }
    }
    //定义队列的头和尾
    private Node<E> head, last;
    //初始化队列长度为0
    private int size = 0;

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }
    //返回第一个元素
    public E peekFirst() {
        return head.element == null ? null : head.getElement();
    }
    //返回最后一个元素
    public E peekLast() {
        return last.element == null ? null : last.getElement();
    }
    //在尾部添加元素
    public void addLast(E element) {
        Node<E> newNode = new Node<E>(element, null);
        //如果为0，则代表队列没有元素
        if (size == 0) {
            head = newNode;
        }else {
            //队列有元素，则将最后一个元素的下一个值设置为新的元素
            last.setNext(newNode);
        }
        //新元素赋值给last
        last = newNode;
        //队列长度+1
        size++;
    }
    //移除并返回第一个元素
    public E removeFirst() {
        //如果为null，直接返回null
        if (isEmpty())
            return null;
        //拿到第一个Node中的元素
        E result = head.getElement();
        //获取第一个Node中的下一个元素并赋值给head
        head = head.getNext();
        //队列长度-1
        size--;
        //判断队列是否为null，如果为null，需要将last置为null
        if (size==0)
            last = null;
        return result;
    }

    public static void main(String[] args) {
        UnThreadSafeQueue<String> queue = new UnThreadSafeQueue<String>();
        queue.addLast("Hello");
        queue.addLast("World");
        queue.addLast("Java");

        System.out.println(queue.removeFirst());
        System.out.println(queue.removeFirst());
        System.out.println(queue.removeFirst());
    }
}
