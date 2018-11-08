package scommons.client.ui.tree

import io.github.shogowada.scalajs.reactjs.React
import io.github.shogowada.scalajs.reactjs.VirtualDOM._
import org.scalatest.Assertion
import scommons.client.test.TestSpec
import scommons.client.ui.tree.BrowseTreeCss._
import scommons.client.ui.tree.TreeCss._
import scommons.client.ui.{ButtonImagesCss, ImageLabelWrapper}
import scommons.client.util.BrowsePath

class BrowseTreeSpec extends TestSpec {

  it should "call onSelect function when select new item" in {
    //given
    val onSelect = mockFunction[BrowseTreeData, Unit]
    val data = BrowseTreeItemData("test", BrowsePath("/test"))
    val props = BrowseTreeProps(List(data), onSelect = onSelect)
    val comp = shallowRender(<(BrowseTree())(^.wrapped := props)())
    val nodeProps = findComponentProps(comp, TreeNode)

    //then
    onSelect.expects(data)

    //when
    nodeProps.onSelect.get()
  }

  it should "not call onSelect function when select the same item" in {
    //given
    val onSelect = mockFunction[BrowseTreeData, Unit]
    val data = BrowseTreeItemData("test", BrowsePath("/test", exact = false))
    val props = BrowseTreeProps(
      roots = List(data),
      selectedItem = Some(data.path.copy(value = "/test/1")),
      onSelect = onSelect
    )
    val comp = shallowRender(<(BrowseTree())(^.wrapped := props)())
    val nodeProps = findComponentProps(comp, TreeNode)
    props.selectedItem.map(_.prefix) shouldBe Some(data.path.prefix)

    //then
    onSelect.expects(_: BrowseTreeData).never()

    //when
    nodeProps.onSelect.get()
  }

  it should "expand node when onExpand" in {
    //given
    val data = BrowseTreeNodeData("test", BrowsePath("/test"))
    val props = BrowseTreeProps(List(data))
    val renderer = createRenderer()
    renderer.render(<(BrowseTree())(^.wrapped := props)())
    val comp = renderer.getRenderOutput()
    val nodeProps = findComponentProps(comp, TreeNode)
    nodeProps.arrowClass shouldBe browseTreeClosedArrow

    //when
    nodeProps.onExpand()

    //then
    val updatedComp = renderer.getRenderOutput()
    findComponentProps(updatedComp, TreeNode).arrowClass shouldBe browseTreeOpenArrow
  }

  it should "collapse initially opened node when onExpand" in {
    //given
    val data = BrowseTreeNodeData("test", BrowsePath("/test", exact = false))
    val props = BrowseTreeProps(
      roots = List(data),
      initiallyOpenedNodes = Set(data.path.copy(value = "/test/1"))
    )
    val renderer = createRenderer()
    renderer.render(<(BrowseTree())(^.wrapped := props)())
    val comp = renderer.getRenderOutput()
    val nodeProps = findComponentProps(comp, TreeNode)
    nodeProps.arrowClass shouldBe browseTreeOpenArrow

    //when
    nodeProps.onExpand()

    //then
    val updatedComp = renderer.getRenderOutput()
    findComponentProps(updatedComp, TreeNode).arrowClass shouldBe browseTreeClosedArrow
  }

  it should "collapse node when onExpand again" in {
    //given
    val data = BrowseTreeNodeData("test", BrowsePath("/test"))
    val props = BrowseTreeProps(List(data))
    val renderer = createRenderer()
    renderer.render(<(BrowseTree())(^.wrapped := props)())
    val comp = renderer.getRenderOutput()
    val nodeProps = findComponentProps(comp, TreeNode)
    nodeProps.arrowClass shouldBe browseTreeClosedArrow
    nodeProps.onExpand()
    val nodePropsV2 = findComponentProps(renderer.getRenderOutput(), TreeNode)
    nodePropsV2.arrowClass shouldBe browseTreeOpenArrow

    //when
    nodePropsV2.onExpand()

    //then
    val nodePropsV3 = findComponentProps(renderer.getRenderOutput(), TreeNode)
    nodePropsV3.arrowClass shouldBe browseTreeClosedArrow
  }

  it should "render selected item" in {
    //given
    val data = BrowseTreeItemData("test", BrowsePath("/test", exact = false))
    val props = BrowseTreeProps(
      roots = List(data),
      selectedItem = Some(data.path.copy(value = "/test/1"))
    )
    val component = <(BrowseTree())(^.wrapped := props)()

    //when
    val result = shallowRender(component)

    //then
    findComponentProps(result, TreeNode).itemClass should include(browseTreeSelectedItem)
  }

  it should "render item with image" in {
    //given
    val data = BrowseTreeItemData("test", BrowsePath("/test"), Some(ButtonImagesCss.folder))
    val props = BrowseTreeProps(List(data), selectedItem = Some(data.path))
    val comp = shallowRender(<(BrowseTree())(^.wrapped := props)())
    val valueWrapper = React.createClass[Unit, Unit] { _ =>
      findComponentProps(comp, TreeNode).renderValue()
    }

    //when
    val result = shallowRender(<(valueWrapper).empty)

    //then
    assertDOMComponent(result, <.div()(
      ImageLabelWrapper(data.image.get, Some(data.text), alignText = false)
    ))
  }

  it should "render item without image" in {
    //given
    val data = BrowseTreeItemData("test", BrowsePath("/test"))
    val props = BrowseTreeProps(List(data), selectedItem = Some(data.path))
    val comp = shallowRender(<(BrowseTree())(^.wrapped := props)())
    val valueWrapper = React.createClass[Unit, Unit] { _ =>
      findComponentProps(comp, TreeNode).renderValue()
    }

    //when
    val result = shallowRender(<(valueWrapper).empty)

    //then
    assertDOMComponent(result, <.div()(
      data.text
    ))
  }

  it should "render opened node" in {
    //given
    val node = BrowseTreeNodeData("test", BrowsePath("/test"))
    val props = BrowseTreeProps(List(node), openedNodes = Set(node.path))
    val component = <(BrowseTree())(^.wrapped := props)()

    //when
    val result = shallowRender(component)

    //then
    findComponentProps(result, TreeNode).arrowClass shouldBe browseTreeOpenArrow
  }

  it should "render initially opened node" in {
    //given
    val node1 = BrowseTreeNodeData("node 1", BrowsePath("/node-1"))
    val node2 = BrowseTreeNodeData("node 2", BrowsePath("/node-2"))
    val props = BrowseTreeProps(List(node1, node2),
      openedNodes = Set(node1.path),
      initiallyOpenedNodes = Set(node2.path))
    val component = <(BrowseTree())(^.wrapped := props)()

    //when
    val result = shallowRender(component)

    //then
    findProps(result, TreeNode).map(_.arrowClass) shouldBe List(
      browseTreeOpenArrow,
      browseTreeOpenArrow
    )
  }

  it should "render opened node when componentWillReceiveProps" in {
    //given
    val node1 = BrowseTreeNodeData("node 1", BrowsePath("/node-1"))
    val prevProps = BrowseTreeProps(List(node1), openedNodes = Set(node1.path))
    val renderer = createRenderer()
    renderer.render(<(BrowseTree())(^.wrapped := prevProps)())
    val comp = renderer.getRenderOutput()
    findComponentProps(comp, TreeNode).arrowClass shouldBe browseTreeOpenArrow
    val node2 = BrowseTreeNodeData("node 2", BrowsePath("/node-2"))
    val props = BrowseTreeProps(List(node1, node2), openedNodes = Set(node2.path))

    //when
    renderer.render(<(BrowseTree())(^.wrapped := props)())

    //then
    findProps(renderer.getRenderOutput(), TreeNode).map(_.arrowClass) shouldBe List(
      browseTreeOpenArrow,
      browseTreeOpenArrow
    )
  }

  it should "not render opened node when it was removed" in {
    //given
    val renderer = createRenderer()
    val node1 = BrowseTreeNodeData("node 1", BrowsePath("/node-1"))
    val props = BrowseTreeProps(List(node1), openedNodes = Set(node1.path))
    renderer.render(<(BrowseTree())(^.wrapped := props)())
    findComponentProps(renderer.getRenderOutput(), TreeNode).arrowClass shouldBe browseTreeOpenArrow
    val node2 = BrowseTreeNodeData("node 2", BrowsePath("/node-2"))
    val propsV2 = BrowseTreeProps(List(node2), openedNodes = Set(node2.path))
    renderer.render(<(BrowseTree())(^.wrapped := propsV2)())
    findComponentProps(renderer.getRenderOutput(), TreeNode).arrowClass shouldBe browseTreeOpenArrow
    val propsV3 = BrowseTreeProps(List(node1, node2))

    //when
    renderer.render(<(BrowseTree())(^.wrapped := propsV3)())

    //then
    findProps(renderer.getRenderOutput(), TreeNode).map(_.arrowClass) shouldBe List(
      browseTreeClosedArrow,
      browseTreeOpenArrow
    )
  }

  it should "render opened child nodes" in {
    //given
    val topItem = BrowseTreeItemData("top item", BrowsePath("/top-item"))
    val childItem = BrowseTreeItemData("child item", BrowsePath("/child-item"))
    val childNode = BrowseTreeNodeData("child node", BrowsePath("/child-node"), children = List(childItem))
    val topNode = BrowseTreeNodeData("top node", BrowsePath("/top-node"), children = List(childNode))
    val props = BrowseTreeProps(List(topItem, topNode), openedNodes = Set(topNode.path, childNode.path))
    val component = <(BrowseTree())(^.wrapped := props)()

    //when
    val result = shallowRender(component)

    //then
    assertDOMComponent(result, <.div(^.className := TreeCss.tree)(), { case List(topItemE, topNodeE) =>
      assertComponent(topItemE, TreeNode) { topItemProps =>
        assertTreeNode(topItemProps, props, topItem)
      }
      assertComponent(topNodeE, TreeNode)({ topNodeProps =>
        assertTreeNode(topNodeProps, props, topNode)
      }, { case List(childNodeE) =>
        assertComponent(childNodeE, TreeNode)({ childNodeProps =>
          assertTreeNode(childNodeProps, props, childNode, level = 1)
        }, { case List(childItemE) =>
          assertComponent(childItemE, TreeNode) { childItemProps =>
            assertTreeNode(childItemProps, props, childItem, level = 2)
          }
        })
      })
    })
  }

  it should "not render closed child nodes" in {
    //given
    val topItem = BrowseTreeItemData("top item", BrowsePath("/top-item"))
    val childItem = BrowseTreeItemData("child item", BrowsePath("/child-item"))
    val childNode = BrowseTreeNodeData("child node", BrowsePath("/child-node"), children = List(childItem))
    val topNode = BrowseTreeNodeData("top node", BrowsePath("/top-node"), children = List(childNode))
    val props = BrowseTreeProps(List(topItem, topNode))
    val component = <(BrowseTree())(^.wrapped := props)()

    //when
    val result = shallowRender(component)

    //then
    assertDOMComponent(result, <.div(^.className := TreeCss.tree)(), { case List(topItemE, topNodeE) =>
      assertComponent(topItemE, TreeNode) { topItemProps =>
        assertTreeNode(topItemProps, props, topItem)
      }
      assertComponent(topNodeE, TreeNode) { topNodeProps =>
        assertTreeNode(topNodeProps, props, topNode)
      }
    })
  }

  private def assertTreeNode(nodeProps: TreeNodeProps,
                             props: BrowseTreeProps,
                             data: BrowseTreeData,
                             level: Int = 0): Assertion = {

    val (expectedIsNode, expectedIsOpened) = data match {
      case _: BrowseTreeItemData => (false, false)
      case _: BrowseTreeNodeData => (true, props.openedNodes.contains(data.path))
    }

    val topItemClass = if (level == 0) browseTreeTopItem else ""
    val topItemImageClass = if (level == 0) browseTreeTopItemImageValue else ""
    val selectedClass = if (props.selectedItem.contains(data.path)) browseTreeSelectedItem else ""
    val expectedNodeClass = if (expectedIsNode) treeNode else ""

    inside (nodeProps) {
      case TreeNodeProps(isNode, paddingLeft, itemClass, nodeClass, nodeIconClass, arrowClass, valueClass, _, _, _) =>
        isNode shouldBe expectedIsNode
        paddingLeft shouldBe (level * 16)
        itemClass shouldBe s"$treeItem $selectedClass $topItemClass"
        nodeClass shouldBe s"$treeItem $expectedNodeClass $topItemImageClass"
        nodeIconClass shouldBe s"$treeItem $treeNodeIcon"
        arrowClass shouldBe (if (expectedIsOpened) browseTreeOpenArrow else browseTreeClosedArrow)
        valueClass shouldBe (if (expectedIsNode) treeNodeValue else treeItemValue)
    }
  }
}
