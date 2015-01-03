#include "Message.h"
#include "Logging.h"

using namespace tox;


template<typename MessageFormat>
MessageFormat &
Message<MessageFormat>::operator << (uint8_t  b)
{
  push_back (b & 0xff);
  return static_cast<MessageFormat &> (*this);
}


template<typename MessageFormat>
MessageFormat &
Message<MessageFormat>::operator << (uint16_t s)
{
  push_back ((s >> 8 * 1) & 0xff);
  push_back ((s >> 8 * 0) & 0xff);
  return static_cast<MessageFormat &> (*this);
}


template<typename MessageFormat>
MessageFormat &
Message<MessageFormat>::operator << (uint32_t l)
{
  push_back ((l >> 8 * 3) & 0xff);
  push_back ((l >> 8 * 2) & 0xff);
  push_back ((l >> 8 * 1) & 0xff);
  push_back ((l >> 8 * 0) & 0xff);
  return static_cast<MessageFormat &> (*this);
}


template<typename MessageFormat>
MessageFormat &
Message<MessageFormat>::operator << (uint64_t q)
{
  push_back ((q >> 8 * 7) & 0xff);
  push_back ((q >> 8 * 6) & 0xff);
  push_back ((q >> 8 * 5) & 0xff);
  push_back ((q >> 8 * 4) & 0xff);
  push_back ((q >> 8 * 3) & 0xff);
  push_back ((q >> 8 * 2) & 0xff);
  push_back ((q >> 8 * 1) & 0xff);
  push_back ((q >> 8 * 0) & 0xff);
  return static_cast<MessageFormat &> (*this);
}


template<typename MessageFormat>
MessageFormat &
Message<MessageFormat>::operator << (MessageFormat const &message)
{
  append (message.cbegin (), message.cend ());
  return static_cast<MessageFormat &> (*this);
}


template struct tox::Message<PlainText>;
template struct tox::Message<CipherText>;


void
PlainText::shift_left (std::size_t offset, std::size_t bit_size)
{
  assert (!empty ());
  assert (bit_size <= 8);

  if (offset == 0 && bit_size == 1)
    {
      assert ((at (size () - 1) & __extension__ 0b11111110) == 0);
      at (size () - 1) <<= 8 - bit_size;
    }
  else if (offset == 1 && bit_size == 7)
    {
      assert ((at (size () - 2) & __extension__ 0b01111111) == 0);
      at (size () - 2) |= at (size () - 1);
      pop_back ();
    }
  else
    {
      printf ("shift_left (%zd, %zd)\n", offset, bit_size);
      abort ();
    }
}
