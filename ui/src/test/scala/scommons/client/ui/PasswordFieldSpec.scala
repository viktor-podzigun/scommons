package scommons.client.ui

import org.scalajs.dom.ext.KeyCode
import scommons.client.ui.PasswordFieldSpec.InputMock
import scommons.react.test._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import scala.scalajs.js.annotation.JSExportAll

class PasswordFieldSpec extends TestSpec with TestRendererUtils {

  it should "call onChange function when input is changed" in {
    //given
    val onChange = mockFunction[String, Unit]
    val props = PasswordFieldProps("test password", onChange)
    val comp = testRender(<(PasswordField())(^.wrapped := props)())
    comp.props.selectDynamic("value") shouldBe props.password
    val newValue = "new password"

    //then
    onChange.expects(newValue)

    //when
    comp.props.onChange(literal(target = literal(value = newValue)))
  }

  it should "call onEnter function when keyCode is Enter" in {
    //given
    val onEnter = mockFunction[Unit]
    val props = PasswordFieldProps("test password", _ => (), onEnter = onEnter)
    val comp = testRender(<(PasswordField())(^.wrapped := props)())
    comp.props.selectDynamic("value") shouldBe props.password

    //then
    onEnter.expects()

    //when
    comp.props.onKeyDown(literal(keyCode = KeyCode.Enter))
  }

  it should "not call onEnter function when keyCode is other than Enter" in {
    //given
    val onEnter = mockFunction[Unit]
    val props = PasswordFieldProps("test password", _ => (), onEnter = onEnter)
    val comp = testRender(<(PasswordField())(^.wrapped := props)())
    comp.props.selectDynamic("value") shouldBe props.password

    //then
    onEnter.expects().never()

    //when
    comp.props.onKeyDown(literal(keyCode = KeyCode.Up))
  }

  it should "render correct props" in {
    //given
    val props = PasswordFieldProps(
      "test password",
      onChange = _ => (),
      className = Some("test-class"),
      placeholder = Some("test-placeholder"),
      readOnly = true
    )

    //when
    val result = testRender(<(PasswordField())(^.wrapped := props)())

    //then
    assertNativeComponent(result, <.input(
      ^("readOnly") := props.readOnly,
      ^.`type` := "password",
      props.className.map(^.className := _),
      props.placeholder.map(^.placeholder := _),
      ^.value := props.password
    )())
  }

  it should "focus input element if requestFocus = true" in {
    //given
    val props = PasswordFieldProps("new password", onChange = _ => (), requestFocus = true)
    val inputMock = mock[InputMock]
    (inputMock.value _).expects().returning("")

    //then
    (inputMock.focus _).expects()

    //when
    testRender(<(PasswordField())(^.wrapped := props)(), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock.asInstanceOf[js.Any]
      else null
    })
  }

  it should "focus input element if requestFocus changed from false to true" in {
    //given
    val prevProps = PasswordFieldProps("test password", onChange = _ => ())
    val inputMock = mock[InputMock]
    (inputMock.value _).expects().returning("")
    val renderer = createTestRenderer(<(PasswordField())(^.wrapped := prevProps)(), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock.asInstanceOf[js.Any]
      else null
    })
    val props = PasswordFieldProps("new password", onChange = _ => (), requestFocus = true)
    props should not be prevProps

    //then
    (inputMock.focus _).expects()

    //when
    TestRenderer.act { () =>
      renderer.update(<(PasswordField())(^.wrapped := props)())
    }
  }

  it should "not focus input element if requestFocus not changed" in {
    //given
    val prevProps = PasswordFieldProps("test password", onChange = _ => ())
    val inputMock = mock[InputMock]
    (inputMock.value _).expects().returning("")
    val renderer = createTestRenderer(<(PasswordField())(^.wrapped := prevProps)(), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock.asInstanceOf[js.Any]
      else null
    })
    val props = PasswordFieldProps("new password", onChange = _ => ())
    props should not be prevProps

    //then
    (inputMock.focus _).expects().never()

    //when
    TestRenderer.act { () =>
      renderer.update(<(PasswordField())(^.wrapped := props)())
    }
  }

  it should "select password if requestSelect = true" in {
    //given
    val props = PasswordFieldProps("new password", onChange = _ => (), requestSelect = true)
    val inputMock = mock[InputMock]
    (inputMock.value _).expects().returning(props.password)

    //then
    (inputMock.setSelectionRange _).expects(0, props.password.length)

    //when
    testRender(<(PasswordField())(^.wrapped := props)(), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock.asInstanceOf[js.Any]
      else null
    })
  }

  it should "select password if requestSelect changed from false to true" in {
    //given
    val prevProps = PasswordFieldProps("test password", onChange = _ => ())
    val inputMock = mock[InputMock]
    (inputMock.value _).expects().returning(prevProps.password)
    val renderer = createTestRenderer(<(PasswordField())(^.wrapped := prevProps)(), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock.asInstanceOf[js.Any]
      else null
    })
    val props = PasswordFieldProps("new password", onChange = _ => (), requestSelect = true)
    props should not be prevProps

    //then
    (inputMock.value _).expects().returning(props.password)
    (inputMock.setSelectionRange _).expects(0, props.password.length)

    //when
    TestRenderer.act { () =>
      renderer.update(<(PasswordField())(^.wrapped := props)())
    }
  }

  it should "not select password if requestSelect not changed" in {
    //given
    val prevProps = PasswordFieldProps("test password", onChange = _ => (), requestSelect = true)
    val inputMock = mock[InputMock]
    (inputMock.value _).expects().returning(prevProps.password)
    (inputMock.setSelectionRange _).expects(0, prevProps.password.length)
    val renderer = createTestRenderer(<(PasswordField())(^.wrapped := prevProps)(), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock.asInstanceOf[js.Any]
      else null
    })
    val props = PasswordFieldProps("new password", onChange = _ => (), requestSelect = true)
    props should not be prevProps

    //then
    (inputMock.value _).expects().never()
    (inputMock.setSelectionRange _).expects(*, *).never()

    //when
    TestRenderer.act { () =>
      renderer.update(<(PasswordField())(^.wrapped := props)())
    }
  }
}

object PasswordFieldSpec {

  @JSExportAll
  trait InputMock {

    def value: String
    def setSelectionRange(start: Int, end: Int): Unit
    def focus(): Unit
  }
}
