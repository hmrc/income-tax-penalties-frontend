package controllers

import utils.IntegrationSpecCommonBase

class PenaltiesControllerISpec extends IntegrationSpecCommonBase{

  val controller: PenaltiesController = injector.instanceOf[PenaltiesController]

  s"return OK" when {

    "the get penalty details call is successful" in {

    }
  }

  s"return NOT_FOUND" when {

    "the get penalty details call is unable to retrieve the data" in {

    }
  }

  s"return INTERNAL_SERVER_ERROR" when {

    "the get penalty details call fails" in {

    }
  }

}
