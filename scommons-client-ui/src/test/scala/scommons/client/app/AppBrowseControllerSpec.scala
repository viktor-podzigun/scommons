package scommons.client.app

import io.github.shogowada.scalajs.reactjs.React
import io.github.shogowada.scalajs.reactjs.VirtualDOM._
import io.github.shogowada.scalajs.reactjs.elements.ReactElement
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.client.app.AppBrowseController.findItemAndPath
import scommons.client.test.TestSpec
import scommons.client.test.raw.MemoryRouter._
import scommons.client.test.raw.ReactTestUtils._
import scommons.client.ui.Buttons
import scommons.client.ui.tree.{BrowseTreeItemData, BrowseTreeNodeData}
import scommons.client.util.{ActionsData, BrowsePath}

class AppBrowseControllerSpec extends TestSpec {

  private val childItemComp = React.createClass[Unit, Unit] { _ =>
    <.p()("childItemComp")
  }
  private val topNodeComp = React.createClass[Unit, Unit] { _ =>
    <.p()("topNodeComp")
  }

  private val childItemPath = BrowsePath("/child-item")
  private val topNodePath = BrowsePath("/top-node")
  private val openedTopNodePath = BrowsePath("/opened-top-node")

  private val buttons = List(Buttons.ADD, Buttons.REMOVE)

  private val childItem = BrowseTreeItemData("child item", childItemPath, None,
    ActionsData.empty.copy(enabledCommands = Set(Buttons.ADD.command)), Some(childItemComp))

  private val topNode = BrowseTreeNodeData("top node", topNodePath, None,
    ActionsData.empty.copy(enabledCommands = Set(Buttons.REMOVE.command)), Some(topNodeComp), List(childItem))

  private val openedTopNode = BrowseTreeNodeData("initially opened top node", openedTopNodePath, None,
    ActionsData.empty.copy(enabledCommands = Set(Buttons.ADD.command)), None)

  private val dispatch: Dispatch = _ => ()
  private val treeRoots = List(topNode, openedTopNode)
  private val initiallyOpenedNodes = Set(openedTopNode.path)

  it should "render component with default (root) path" in {
    //given
    val component = createAppBrowseController(BrowsePath("/"))

    //when
    val result = renderIntoDocument(component)

    //then
    assertRenderedProps(result, ActionsData.empty, None)
    scryRenderedComponentsWithType(result, childItemComp).length shouldBe 0
    scryRenderedComponentsWithType(result, topNodeComp).length shouldBe 0
  }

  it should "render component with selected top node path" in {
    //given
    val component = createAppBrowseController(topNodePath)

    //when
    val result = renderIntoDocument(component)

    //then
    assertRenderedProps(result, topNode.actions, Some(topNode.path))
    scryRenderedComponentsWithType(result, childItemComp).length shouldBe 0
    scryRenderedComponentsWithType(result, topNodeComp).length should be > 0
  }

  it should "render component with selected child path" in {
    //given
    val component = createAppBrowseController(childItemPath)

    //when
    val result = renderIntoDocument(component)

    //then
    assertRenderedProps(result, childItem.actions, Some(childItem.path), Set(topNode.path))
    scryRenderedComponentsWithType(result, childItemComp).length should be > 0
    scryRenderedComponentsWithType(result, topNodeComp).length shouldBe 0
  }

  it should "re-render component when selected item changes" in {
    //given
    val comp = renderIntoDocument(createAppBrowseController(topNodePath))
    assertRenderedProps(comp, topNode.actions, Some(topNode.path))
    scryRenderedComponentsWithType(comp, topNodeComp).length should be > 0
    val treeProps = findRenderedComponentProps(comp, AppBrowsePanel).treeProps

    //when
    treeProps.onSelect(childItem)

    //then
    assertRenderedProps(comp, childItem.actions, Some(childItem.path), Set(topNode.path))
    scryRenderedComponentsWithType(comp, childItemComp).length should be > 0
  }

  it should "return item path when findItemAndPath" in {
    //given
    val item1 = BrowseTreeItemData("item1", BrowsePath("/item1"))
    val item2 = BrowseTreeItemData("item2", BrowsePath("/item2"))
    val item3 = BrowseTreeItemData("item3", BrowsePath("/item3"))
    val node1 = BrowseTreeNodeData("node1", BrowsePath("/node1"))
    val node2 = BrowseTreeNodeData("node2", BrowsePath("/node2"), children = List(item3))
    val node3 = BrowseTreeNodeData("node3", BrowsePath("/node3"), children = List(item2, node2))
    val roots = List(node1, node3, item1)

    //when & then
    findItemAndPath(roots, BrowsePath("/unknown")) shouldBe None
    findItemAndPath(roots, item1.path) shouldBe Some(item1 -> Nil)
    findItemAndPath(roots, item2.path) shouldBe Some(item2 -> List(node3))
    findItemAndPath(roots, item3.path) shouldBe Some(item3 -> List(node3, node2))
    findItemAndPath(roots, node1.path) shouldBe Some(node1 -> Nil)
    findItemAndPath(roots, node2.path) shouldBe Some(node2 -> List(node3))
    findItemAndPath(roots, node3.path) shouldBe Some(node3 -> Nil)
  }

  private def createAppBrowseController(targetPath: BrowsePath): ReactElement = {
    <.MemoryRouter(^.initialEntries := List(targetPath.value))(
      <(AppBrowseController())(^.wrapped := AppBrowseControllerProps(
        buttons,
        treeRoots,
        dispatch,
        initiallyOpenedNodes
      ))()
    )
  }

  private def assertRenderedProps(result: Instance,
                                  expectedActions: ActionsData,
                                  expectedSelectedItem: Option[BrowsePath],
                                  expectedOpenedNodes: Set[BrowsePath] = Set.empty): Unit = {

    val browsePanelProps = findRenderedComponentProps(result, AppBrowsePanel)
    val buttonsProps = browsePanelProps.buttonsProps
    buttonsProps.buttons shouldBe buttons
    buttonsProps.actions shouldBe expectedActions
    buttonsProps.dispatch shouldBe dispatch
    buttonsProps.group shouldBe true

    val treeProps = browsePanelProps.treeProps
    treeProps.roots shouldBe treeRoots
    treeProps.selectedItem shouldBe expectedSelectedItem
    treeProps.openedNodes shouldBe expectedOpenedNodes
    treeProps.initiallyOpenedNodes shouldBe initiallyOpenedNodes
  }
}
