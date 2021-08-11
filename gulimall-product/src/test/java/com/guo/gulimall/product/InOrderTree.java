package com.guo.gulimall.product;

public class InOrderTree {
    class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;
        public TreeNode() {
        }
        public TreeNode(int val){
        }

        public TreeNode(int val, TreeNode left, TreeNode right) {
        }
    }

    public static void main(String[] args) {
        int[] inOrder = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    }

    public TreeNode buildTree(int[] preorder, int[] inorder) {
        return build(preorder, 0, preorder.length -1, inorder, 0, inorder.length - 1);
    }

    TreeNode build (int[] preorder, int preStart, int preEnd, int[] inorder, int inStart, int inEnd){
        if(preStart > preEnd || inStart > inEnd){
            return null;
        }
        int index = 0;
        int rootVal = preorder[preStart];
        // 注意等号
        for(int i = inStart;i <= inEnd;i++){
            if(inorder[i] == rootVal){
                index = i;
            }
        }
        int leftSize = index - inStart;
        TreeNode root = new TreeNode(rootVal);
        root.left = build(preorder, preStart + 1, preStart + leftSize, inorder, inStart, index );
        root.right = build(preorder, preStart + leftSize + 1, preEnd, inorder, index + 1, inEnd);
        return root;
    }

}
