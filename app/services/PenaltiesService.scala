package services

import connectors.PenaltiesConnector
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}

@Singleton
class PenaltiesService @Inject()(
                                val penaltiesConnector: PenaltiesConnector
                                ){

}
