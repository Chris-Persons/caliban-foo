package caliban


import scala.language.postfixOps
import caliban.StackService.StackService
import caliban.GraphQL.graphQL
import caliban.schema.Annotations.{GQLDeprecated, GQLDescription}
import caliban.schema.GenericSchema
import caliban.wrappers.ApolloTracing.apolloTracing
import caliban.wrappers.Wrappers.{maxDepth, maxFields, printSlowQueries, timeout}
import zio.URIO
import zio.clock.Clock
import zio.console.Console
import zio.duration._
import zio.stream.ZStream

object StackApi extends GenericSchema[StackService] {

  case class Queries(
                      numbers: URIO[StackService, List[Int]],
                      head: URIO[StackService, Option[Int]]
                    )

  case class Mutations(
                        push: Int => URIO[StackService, Unit],
                        pop: URIO[StackService, Option[Int]]
                      )

  case class Subscriptions(numbersDeleted: ZStream[StackService, Nothing, Int])

  val api: GraphQL[Console with Clock with StackService] =
    graphQL(
      RootResolver(
        Queries(
          StackService.getStack,
          StackService.peak),
        Mutations(
          args => StackService.push(args),
          StackService.pop
        ),
        Subscriptions(StackService.deletedEvents)
      )
    ) @@
      maxFields(200) @@ // query analyzer that limit query fields
      maxDepth(30) @@ // query analyzer that limit query depth
      timeout(3 seconds) @@ // wrapper that fails slow queries
      printSlowQueries(500 millis) @@ // wrapper that logs slow queries
      apolloTracing // wrapper for https://github.com/apollographql/apollo-tracing

}
