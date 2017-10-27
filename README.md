# RayScript
Java interpreter for my own scripting language

Here is some example code:


    function fib(n)
    {
      var ret =1;

      if (n.le(2)) # mit gt gehts
      {
          ret = 1;
      }
      else
      {
          var a = this.fib(n.minus(1) );
          var b = this.fib(n.minus(2) );
          ret = a.plus(b);
      }
      ret.copy();
    }

    function fakultät(n)
    {
      var ret = 1;
      if(n.gt(1))
      {
        ret= n.times( this.fakultät(n.minus(1)) );
      }
      ret.copy();
    }

    class Ray
    {
      var cv;
      function init( p)
      {
         cv = p
      }
      function append(a)
      {
        cv.append!(a);
      }
      function getV()
      {
        cv;
      }
    }

    code
    {
      var r = new Ray(55);
      r.append("asfd");
      r.getV().print();
      this.fib(14).print();
      this.fakultät(5).print();

      var a = "as3s";
      if( a.eq("a2s"))
      {
        "if".print();
      }
      else if (a.eq("as") )
      {

        "else if".print();
      }
      else
      {
        "else".print();
      }

      var as = 5;

      var b = 324;
      "$as   cool  $b".print();


      var str = new String();
      str.set("raqs");
      str.append!("test");
      str.print();
    }
