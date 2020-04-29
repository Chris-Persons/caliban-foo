package caliban

import zio.stream.ZStream
import zio.{Has, Queue, Ref, UIO, URIO, ZLayer}

object StackService {

  type StackService = Has[Service]

  trait Service {
    def getStack: UIO[List[Int]]

    def push(num: Int): UIO[Unit]

    def pop: UIO[Option[Int]]

    def peak: UIO[Option[Int]]

    def deletedEvents: ZStream[Any, Nothing, Int]
  }

  def getStack: URIO[StackService, List[Int]] = URIO.accessM(_.get.getStack)

  def push(num: Int): URIO[StackService, Unit] = URIO.accessM(_.get.push(num))

  def pop: URIO[StackService, Option[Int]] = URIO.accessM(_.get.pop)

  def peak: URIO[StackService, Option[Int]] = URIO.accessM(_.get.peak)

  def deletedEvents: ZStream[StackService, Nothing, Int] =
    ZStream.accessStream(_.get.deletedEvents)

  def make(initial: List[Int]): ZLayer[Any, Nothing, StackService] = ZLayer.fromEffect {
    for {
      nums <- Ref.make(initial)
      subscribers <- Ref.make(List.empty[Queue[Int]])
    } yield new Service {

      def getStack: UIO[List[Int]] = nums.get

      def push(num: Int): UIO[Unit] = nums.modify(list => ((), num :: list)).tap(
        _
        => UIO.when(true)(
          subscribers.get.flatMap(
            UIO.foreach(_)(
              queue =>
                queue.offer(num).onInterrupt(
                  subscribers.update(_.filterNot(_ == queue))
                ))

          ))
      )

      def pop: UIO[Option[Int]] = nums.modify(list => if (list.isEmpty) (None, list) else (Some(list.head), list)).tap(
        deleted => UIO.when(deleted.nonEmpty)(
          subscribers.get.flatMap(
            // add item to all subscribers
            UIO.foreach(_)(
              queue =>
                queue
                  .offer(deleted.get)
                  .onInterrupt(
                    subscribers.update(_.filterNot(_ == queue))
                  ) // if queue was shutdown, remove from subscribers
            )
          ))
      )

      def peak: UIO[Option[Int]] = nums.get.map(_.headOption)

      def deletedEvents: ZStream[Any, Nothing, Int] = ZStream.unwrap {
        for {
          queue <- Queue.unbounded[Int]
          _     <- subscribers.update(queue :: _)
        } yield ZStream.fromQueue(queue)
      }
    }
  }
}
